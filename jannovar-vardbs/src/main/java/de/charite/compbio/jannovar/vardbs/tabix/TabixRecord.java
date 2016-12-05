package de.charite.compbio.jannovar.vardbs.tabix;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;

public class TabixRecord {

	// Fields up to the INFO column

	/** Name of the chromosome */
	final private String chrom;
	/** Position of the variant, 0-based */
	final private int pos;
	/** Reference sequence */
	final private String ref;
	/** Alternative alleles in cluster */
	final private ImmutableList<String> alt;
	/** Filters, NC: inconsistent genotype submission for at least one sample */
	final private ImmutableList<String> filter;

	// Entries of the INFO column

	/** Fields */
	final private ImmutableSortedMap<String, ImmutableList<String>> fields;

	public TabixRecord(String chrom, int pos, String ref, List<String> alt, Collection<String> filter,
			Map<String, List<String>> fields) {
		this.chrom = chrom;
		this.pos = pos;
		this.ref = ref;
		this.alt = ImmutableList.copyOf(alt);
		this.filter = ImmutableList.copyOf(filter);

		ImmutableSortedMap.Builder<String, ImmutableList<String>> fieldBuilder = ImmutableSortedMap.naturalOrder();
		for (Entry<String, List<String>> e : fields.entrySet())
			fieldBuilder.put(e.getKey(), ImmutableList.copyOf(e.getValue()));
		this.fields = fieldBuilder.build();

	}

	public String getChrom() {
		return chrom;
	}

	public int getPos() {
		return pos;
	}

	public String getRef() {
		return ref;
	}

	public ImmutableList<String> getAlt() {
		return alt;
	}

	public ImmutableList<String> getFilter() {
		return filter;
	}

	public ImmutableSortedMap<String, ImmutableList<String>> getFields() {
		return fields;
	}

	@Override
	public String toString() {
		return "Tabix [chrom=" + chrom + ", pos=" + pos + ", ref=" + ref + ", alt=" + alt + ", filter=" + filter
				+ ", fields=" + fields + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + ((alt == null) ? 0 : alt.hashCode());
		result = prime * result + ((chrom == null) ? 0 : chrom.hashCode());
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
		TabixRecord other = (TabixRecord) obj;
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
		if (chrom == null) {
			if (other.chrom != null)
				return false;
		} else if (!chrom.equals(other.chrom))
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
