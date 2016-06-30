package de.charite.compbio.jannovar.cmd.hgvs2vcf;

/**
 * Represent a HGVS variants
 * Define the HGVS fields
 * 
 * **/
public class HGVS {
	/** define the transcript code, its format such as: NC_000008.10 **/
   private String transcriptcode;
   /** define simple transcript code, it is part of transcriptcode. its format such as: NC_000008 **/
   private String transcriptcode_2;
   /** define the reference sequence, such as c: coding DNA reference sequence   g: genomic reference sequence */
   private char type;
   /** define the offset of this variant in the transcript region**/
   private int offset;
   /** define the length of the variants, for substitution, interval is 1, otherwise >1*/
   private int interval=1;
   /** define the reference allele of the variants in the HGVS code*/
   private String ref;
   /** define the variant allele of the variants in the HGVS code*/
   private String alt;
   /** define the change type of the variants in the HGVS code, SUBSTRITUTION, DEL, INS....*/
   private String change;
   /** define the hgvs name , e.g NM_017837.3:c.1022C>A*/
   private String hgvs;
   /** used for DUP, define the replicate times*/
   private int repeats=1;
   /** used for the variants in the intron, define the distance from the cds transcript*/
   private int intron=0;
   /** used for marking whether the hgvs code parse into this class correctly*/
   public boolean correct=false;
   
   /* constructor for this class **/
   public HGVS(String hgvs){
	    this.hgvs=hgvs;
	    this.correct= SetFields();;
   }
   public boolean isCorrect(){
	   return this.correct;
   }
   public String getTranscriptCode(){
	    return this.transcriptcode;
   }
   
   public String getTranscriptCode2(){
	    return this.transcriptcode_2;
  }
  
   public char getTranscriptType(){
	    return this.type;
   }
   
   public String getRefAllele(){
	   return this.ref;
   }
   public String getAltAllele(){
	   return this.alt;
   }
   public String getChange(){
	   
	   return this.change;
   }
   public String getName(){
	   return this.hgvs;
   }
   
  public int getRepeat(){
	   
	   return this.repeats;
   }
   public int getOffset(){
	   return this.offset;
   }
   public int getInterval(){
	   return this.interval;
   }
   public int getIntron(){
	   return this.intron;
   }
   public void setTranscript(String code){
	   System.err.println("Warning: "+this.transcriptcode+" does not exist,"+ "now it is suggestd as "+code+"!");
	   this.transcriptcode=code;
	   
   }
   
   private boolean SetFields(){
	   String hgvs_infos[]=hgvs.split(":");
	   if(hgvs_infos.length<2){
		   System.err.println("Wrong Transcript Format");
		   return false;
	   }
	   this.transcriptcode=hgvs_infos[0];
	   this.transcriptcode_2=hgvs_infos[0].split("\\.")[0];
	   String infos[]=hgvs_infos[1].split("\\.");
	   /** incomplete code */
	   if(infos.length<2){
		   System.err.println("Warning: "+"Wrong Transcript Format");
		   return false;
	   }
	   /**set type */
	   this.type=infos[0].charAt(0);
	   
	   /** If this variant in the intron, extract all infos*/
	   if(infos[1].contains("+")||infos[1].contains("-")){
		   String[] pos_infos=infos[1].split("-|\\+");
		   this.offset=Integer.valueOf(pos_infos[0].replaceAll("[^0-9]+", ""));
		   if(infos[1].contains("+")){
			   this.intron=Integer.valueOf(pos_infos[1].split("A|C|G|T")[0]);
		   }else{
			   this.intron=-Integer.valueOf(pos_infos[1].split("A|C|G|T")[0]);
		   }
		   if(infos[1].contains(">")){
			   String[] infos2=infos[1].split(">|-|\\+");
			   this.ref=infos2[1].substring((int)Math.log10(Math.abs(this.intron))+1);
			   this.alt=String.valueOf(infos2[2]);
			   this.change="Substitutions";
			   return true;
		   }else{
			   System.err.println("Warning: "+this.hgvs+"\t"+this.ref+"\t"+this.alt+"\t"+this.offset+"\t"+this.intron+" is in the intron, currently it won't work for uncharacterised breakpoints");
		       return false;
		   }   
		   
	   }
	   /**if it is a substrituion*/
	   else if(infos[1].contains(">")){
		   
		   String[] change_infos=infos[1].split("A|C|G|T");
		   try{
			   this.offset=Integer.valueOf(change_infos[0].replaceAll("[^0-9]+", "")).intValue();
		   }catch(NumberFormatException e){
			   System.err.println("Warning: "+change_infos[0]+" cannot parse into Integer!");
			   return false;
		   }
		   String[] infos2=infos[1].split(">");
		   this.ref=infos2[0].substring((int)Math.log10(this.offset)+1);
		   this.alt=String.valueOf(infos2[1]);
		   this.change="Substitutions";
	   }else {
		   String[] change_infos=null;
		   if(infos[1].contains("delins")){
			   change_infos=infos[1].split("delins");
			   this.alt=change_infos[1];
			   this.change="DELINS";
		   
	   }else if(infos[1].contains("dup")){
		   		change_infos=infos[1].split("dup");
		   		 this.change="DUP";		   
	   }else if(infos[1].matches("\\d+_\\d+\\[\\d+\\]*")){
		     change_infos=infos[1].split("[|]");
		     try{
		       this.repeats=Integer.valueOf(change_infos[1]).intValue();
		     }catch(NumberFormatException e){
		    	 System.err.println("warning: "+change_infos[1]+" cannot parse into Integer!");
		    	 return false;
		     }
	   		 this.change="DUP";		
	   }else if(infos[1].contains("del")){
		   		change_infos=infos[1].split("del");
		   		this.change="DEL";
		   
	   }else if(infos[1].contains("ins")){
		   		change_infos=infos[1].split("ins");
		   		this.alt=change_infos[1];
		   		this.change="INS";
	   }else if(infos[1].contains("inv")){
		   change_infos=infos[1].split("inv");
	   		this.change="INV";
	   }else if(infos[1].contains("con")){
		   change_infos=infos[1].split("ins");
	   		this.change="INS";
	   }else{
		   System.err.println("Warning: Unknown format, it is skipped!");
		   return false;   
	   }
	   String pos_infos[]=change_infos[0].split("_");
	   try{
		   this.offset=Integer.valueOf(pos_infos[0]).intValue();
		   if(pos_infos.length>1){
			   this.interval=Integer.valueOf(pos_infos[1]).intValue()-Integer.valueOf(pos_infos[0]).intValue()+1;
		   }
	    }catch(NumberFormatException e){
			 System.err.println("Warning: "+pos_infos[0]+" or "+pos_infos[1]+" cannot parse into Integer!");
			 return false;
		}
	   }
	   return true;
	  }
   
   }
   

