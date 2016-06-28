package de.charite.compbio.jannovar.cmd.hgvs_to_genomic;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.CDSPosition;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.ProjectionException;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.reference.TranscriptProjectionDecorator;


public class HGVS_Parser {
	JannovarData data;
	java.util.List<HGVS> failedlist=new java.util.ArrayList<HGVS>();
	String chr;
	int position;
	String ref;
	String alt;
	public HGVS_Parser(JannovarData data){
		this.data=data;
	}
	
	public boolean parse (HGVS hgvs){
		java.util.Map<Character,Character> dict=new java.util.HashMap<Character,Character>();
		dict.put('A', 'T');
		dict.put('T', 'A');
		dict.put('C', 'G');
		dict.put('G', 'C');
		if(!hgvs.isCorrect()){return false;}		
		if(hgvs.getTranscriptType()!='c'){System.err.println("Warning: currently I am only working on HGVS cDNA! ~_~");return false;}
		try {
			TranscriptModel m=null;
			if(data.getTmByAccession().containsKey(hgvs.getTranscriptCode())){
				 m=(TranscriptModel)data.getTmByAccession().get(hgvs.getTranscriptCode());
			}else if(data.getTmByAccession().containsKey(hgvs.getTranscriptCode2())){
				 m=(TranscriptModel)data.getTmByAccession().get(hgvs.getTranscriptCode2());
			}else{
				failedlist.add(hgvs);
				System.err.println("Warning: "+hgvs.getTranscriptCode()+" and "+hgvs.getTranscriptCode2() +" do not exist in the current library!");
				return false;
			}	
		    CDSPosition cds = new CDSPosition(m, hgvs.getOffset(), PositionType.ONE_BASED);
			TranscriptProjectionDecorator tpd=new TranscriptProjectionDecorator(m);
			GenomePosition gpos;
			
			gpos = tpd.cdsToGenomePos(cds);
	
			int start=gpos.toString().indexOf(".")+1;
			this.chr=gpos.toString().substring(0,gpos.toString().indexOf(":"));
			int position=Integer.valueOf(gpos.toString().substring(start)).intValue();
			
		    String newref=tpd.getCDSTranscript().substring(hgvs.getOffset()-1,hgvs.getOffset()+hgvs.getInterval()-1);
		    String newalt=hgvs.getAltAllele();		    
		    if(hgvs.getChange().equals("DEL")||hgvs.getChange().equals("DELINS")){
		    	 if(m.getStrand().isForward()){
		    		 newalt=tpd.getCDSTranscript().substring(hgvs.getOffset()-2,hgvs.getOffset()-1);
		             newref=newalt+newref;
		         }else{
		        	 position=position-hgvs.getInterval();
		        	 newalt=tpd.getCDSTranscript().substring(hgvs.getOffset()+hgvs.getInterval()-1,hgvs.getOffset()+hgvs.getInterval());
			         newref=newalt+new StringBuffer(tpd.getCDSTranscript().substring(hgvs.getOffset()-1,hgvs.getOffset()+hgvs.getInterval()-1)).reverse().toString();
		         }
		    	 if(hgvs.getChange().equals("DELINS")){
		    		 if(m.getStrand().isForward()){
		    			 newalt=newalt+hgvs.getAltAllele();
		    		 }else{
		    			 newalt=newalt+new StringBuffer(hgvs.getAltAllele()).reverse().toString();
		    		 }
		    	 }
		    	 
		    }
		    if(hgvs.getChange().equals("INS")){
		    	if(m.getStrand().isForward()){
		             newref=tpd.getCDSTranscript().substring(hgvs.getOffset()-1,hgvs.getOffset());
		             newalt=newref+newalt;
		        }else{
		        	 position=position-hgvs.getInterval();
		        	 newref=tpd.getCDSTranscript().substring(hgvs.getOffset()+hgvs.getInterval()-1,hgvs.getOffset()+hgvs.getInterval());
		             newalt=newref+new StringBuffer(newalt).reverse().toString();
		        }
		    }
		    if(hgvs.getChange().equals("DUP")){
		    	StringBuffer alt_append=new StringBuffer(); 
		    	for(int i=0;i<hgvs.getRepeat();i++){
		    		alt_append.append(tpd.getCDSTranscript().substring(hgvs.getOffset()-1,hgvs.getOffset()+hgvs.getInterval()-1));
		         }   
		    	if(m.getStrand().isForward()){
		    		newref=tpd.getCDSTranscript().substring(hgvs.getOffset()-2,hgvs.getOffset()-1);
		    		newalt=newref+alt_append.toString();
		    	}else{
		    		position=position-hgvs.getInterval();
		    		newref=tpd.getCDSTranscript().substring(hgvs.getOffset()+hgvs.getInterval()-1,hgvs.getOffset()+hgvs.getInterval());
		    		newalt=newref+alt_append.reverse().toString();
		    	}
		    }
		    
		    if(m.getStrand().isReverse()){
		    	StringBuffer tempalt=new StringBuffer();
		    	for(int j=0;j<newalt.length();j++){
		    		tempalt.append(dict.get(newalt.charAt(j)));
		    	}
		    	newalt=tempalt.toString();
		    	tempalt.setLength(0);
		    	StringBuffer tempref=new StringBuffer();
		    	for(int j=0;j<newref.length();j++){
		    		tempref.append(dict.get(newref.charAt(j)));
		    	}
		    	newref=tempref.toString();
		    	tempref.setLength(0); 
		    }  
			this.position=position;
			this.ref=newref;
			this.alt=newalt;
			
		} catch (ProjectionException e) {
			e.printStackTrace();
			return false;
		}
	 return true;
}
}
