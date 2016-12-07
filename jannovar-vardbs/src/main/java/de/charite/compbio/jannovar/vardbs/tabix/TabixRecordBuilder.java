package de.charite.compbio.jannovar.vardbs.tabix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabixRecordBuilder {
	/** Name of the chromosome */
	private String contig;
	/** Position of the variant, 0-based */
	private int pos;
	/** Reference sequence */
	private String ref;
	/** Alternative alleles in cluster */
	private ArrayList<String> alt;
	/** Filters, NC: inconsistent genotype submission for at least one sample */
	private ArrayList<String> filter;

	/** Allele counts for each population */
	private Map<String, List<String>> fields;

	TabixRecordBuilder() {
		contig = null;
		pos = -1;
		ref = null;
		alt = new ArrayList<>();
		filter = new ArrayList<>();
		fields = new HashMap<>();
	}

	public TabixRecord build() {
		return new TabixRecord(contig, pos, ref, alt, filter, fields);
	}

	public String getContig() {
		return contig;
	}

	public void setContig(String contig) {
		this.contig = contig;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public ArrayList<String> getAlt() {
		return alt;
	}

	public void setAlt(ArrayList<String> alt) {
		this.alt = alt;
	}

	public ArrayList<String> getFilter() {
		return filter;
	}

	public void setFilter(ArrayList<String> filter) {
		this.filter = filter;
	}

	public Map<String, List<String>> getFields() {
		return fields;
	}

	public void setFields(Map<String, List<String>> fields) {
		this.fields = fields;
	}

	@Override
	public String toString() {
		return "CADDRecordBuilder [chrom=" + contig + ", pos=" + pos + ", ref=" + ref + ", alt=" + alt + ", filter="
				+ filter + ", fields=" + fields + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + ((alt == null) ? 0 : alt.hashCode());
		result = prime * result + ((contig == null) ? 0 : contig.hashCode());
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + pos;
		result = prime * result + ((ref == null) ? 0 : ref.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TabixRecordBuilder other = (TabixRecordBuilder) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (alt == null) {
			if (other.alt != null)
				return false;
		} else if (!alt.equals(other.alt))
			return false;
		if (contig == null) {
			if (other.contig != null)
				return false;
		} else if (!contig.equals(other.contig))
			return false;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		if (pos != other.pos)
			return false;
		if (ref == null) {
			if (other.ref != null)
				return false;
		} else if (!ref.equals(other.ref))
			return false;
		return true;
	}

}
