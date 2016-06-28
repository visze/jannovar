package de.charite.compbio.jannovar.cmd.hgvs_to_genomic;

public class HGVS {
   private String transcriptcode;
   private String transcriptcode_2;
   private char type;
   private int offset;
   private int interval=1;
   private String ref;
   private String alt;
   private String change;
   private String hgvs;
   private int repeats=1;
   public boolean correct=false;
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
   
  public int getRepeat(){
	   
	   return this.repeats;
   }
   public int getOffset(){
	   return this.offset;
   }
   public int getInterval(){
	   return this.interval;
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
	   if(infos.length<2){
		   System.err.println("Warning: "+"Wrong Transcript Format");
		   return false;
	   }
	   this.type=infos[0].charAt(0);
	   if(infos[1].contains("+")||infos[1].contains("-")){
		   System.err.println("Warning: "+this.hgvs+" is in the intron, currently it won't work for uncharacterised breakpoints");
		   return false;
	   }
	   if(infos[1].contains(">")){
		   
		   String[] change_infos=infos[1].split("A|C|G|T");
		   try{
			   this.offset=Integer.valueOf(change_infos[0]).intValue();
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
   

