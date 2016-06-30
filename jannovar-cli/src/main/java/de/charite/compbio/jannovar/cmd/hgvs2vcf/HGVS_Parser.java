package de.charite.compbio.jannovar.cmd.hgvs2vcf;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.impl.util.DNAUtils;
import de.charite.compbio.jannovar.reference.CDSPosition;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.ProjectionException;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.reference.TranscriptProjectionDecorator;

/** A  parser to parse HGVS to Genomic**/

public class HGVS_Parser {
	private JannovarData data;
	public GenomeVariant variant=null;
    public String ref;
    public String alt;
    int position;
    String chr;
    HGVS hgvs;
    boolean corrected=false;
	public HGVS_Parser(JannovarData data){
		this.data=data;
	}
	/** parse the HGVS ID to Genomic position, it only works for cdna
	 *  
	 * 
	 *  return GenomeVariant
	 * **/
	public boolean parse (HGVS hgvs){	
		this.hgvs=hgvs;
		/** it only works for cdna**/
		if(hgvs.getTranscriptType()!='c'){
			System.err.println("Warning: currently I am only working on HGVS cDNA! ~_~");
			return false;
		}
		try {
			TranscriptModel m=null;
			if(data.getTmByAccession().containsKey(hgvs.getTranscriptCode())){
				 m=(TranscriptModel)data.getTmByAccession().get(hgvs.getTranscriptCode());
			}else if(data.getTmByAccession().containsKey(hgvs.getTranscriptCode2())){
				 m=(TranscriptModel)data.getTmByAccession().get(hgvs.getTranscriptCode2());
			}else{
				System.err.println("Warning: "+hgvs.getTranscriptCode()+" and "+hgvs.getTranscriptCode2() +" do not exist in the current library!");
				return false;
			}	
		    
			TranscriptProjectionDecorator tpd=new TranscriptProjectionDecorator(m);
			CDSPosition cds = new CDSPosition(m,hgvs.getOffset(), PositionType.ONE_BASED);
			GenomePosition gpos = tpd.cdsToGenomePos(cds);
			
			/**get the offset in a transcript **/
				    
		    String newref=tpd.getCDSTranscript().substring(hgvs.getOffset()-1,hgvs.getOffset()+hgvs.getInterval()-1);
		    String newalt=hgvs.getAltAllele();
		    
		    /** Find out the chr pos ref alt of corresponding genomic poisition
		     *  considering Forward and Reverse Stand
		     *  considering different types of variants, DEL, DELINS, INS, DUP, Substitution
		     * **/
		    if(hgvs.getIntron()!=0&&hgvs.getChange().equals("Substitutions")){
		    	newref=hgvs.getRefAllele();
		    }
		    /*If it is indel, it has to figure out the referrence allele and alternate allele by left-alignment */
		    if(hgvs.getChange().equals("DEL")||hgvs.getChange().equals("DELINS")){
		    	 if(m.getStrand().isForward()){
		    		 newalt=tpd.getCDSTranscript().substring(hgvs.getOffset()-2,hgvs.getOffset()-1);
		             newref=newalt+newref;
		         }else{
		        	 newalt=tpd.getCDSTranscript().substring(hgvs.getOffset()+hgvs.getInterval()-1,hgvs.getOffset()+hgvs.getInterval());
			         newref+=newalt;
		         }
		    	 if(hgvs.getChange().equals("DELINS")){
		    		 if(m.getStrand().isForward()){
		    			 newalt=newalt+hgvs.getAltAllele();
		    		 }else{
		    			 newalt=hgvs.getAltAllele()+newalt;
		    		 }
		    	 }		    	
		    }
		    if(hgvs.getChange().equals("INS")){
		    	if(m.getStrand().isForward()){
		             newref=tpd.getCDSTranscript().substring(hgvs.getOffset()-1,hgvs.getOffset());
		        }else{
		        	 newref=tpd.getCDSTranscript().substring(hgvs.getOffset()+hgvs.getInterval()-1,hgvs.getOffset()+hgvs.getInterval());
		        }
		    	 newalt=newref+newalt;
		    }
		    if(hgvs.getChange().equals("DUP")){
		    	StringBuffer alt_append=new StringBuffer(tpd.getCDSTranscript().substring(hgvs.getOffset()-1,hgvs.getOffset()+hgvs.getInterval()-1)); 
		    	if(!alt_append.toString().equals(hgvs.getAltAllele())){
		    		System.err.println("warning: "+hgvs.getName()+" is not correct!"+ hgvs.getAltAllele()+" cannot find, but a "+alt_append.toString());
		    	}
		    	for(int i=0;i<hgvs.getRepeat();i++){
		    		alt_append.append(alt_append);
		        }   
		    	if(m.getStrand().isForward()){
		    		newref=tpd.getCDSTranscript().substring(hgvs.getOffset()-2,hgvs.getOffset()-1);
		    		newalt=newref+alt_append.toString();
		    		
		    	}else{
		    		newref=tpd.getCDSTranscript().substring(hgvs.getOffset()+hgvs.getInterval()-1,hgvs.getOffset()+hgvs.getInterval());
		    		newalt=alt_append.toString()+newref;		    		
		    	}
		    }
		    
		    /** get the genomic position
		     *  save all to GenomeVariant
		     * **/
		    if(hgvs.getIntron()==0){
		    	this.variant=new GenomeVariant(gpos,newref,newalt,m.getStrand());  /* ref is reverse or forward ??**/
		    }else{
		    	this.variant=new GenomeVariant(gpos.shifted(hgvs.getIntron()),newref,newalt,m.getStrand());
		    }
		    /** translate this variants with strand forward */
		    this.variant=variant.withStrand(Strand.FWD);
		    this.alt=newalt;
		    this.ref=newref;
		    if(m.getStrand().isReverse()){
		    	this.ref=DNAUtils.reverseComplement(newref);
		        this.alt=DNAUtils.reverseComplement(newalt);
		    }		    
		} catch (ProjectionException e) {
			e.printStackTrace();
			return false;
		}
	 return true;
}
}
