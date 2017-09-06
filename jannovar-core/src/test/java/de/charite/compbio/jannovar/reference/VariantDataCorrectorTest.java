package de.charite.compbio.jannovar.reference;

import org.junit.Assert;
import org.junit.Test;

import de.charite.compbio.jannovar.reference.VariantDataCorrector;

/**
 * Test for the VariantCorrectorData class.
 *
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 */
public class VariantDataCorrectorTest {

	/** Test with SNV data. */
	@Test
	public void testSingleNucleotide() {
		VariantDataCorrector corr = new VariantDataCorrector("C", "T", 100);
		Assert.assertEquals(corr.getPosition(), 100);
		Assert.assertEquals(corr.getRef(), "C");
		Assert.assertEquals(corr.getAlt(), "T");
	}

	/** Test with insertion data. */
	@Test
	public void testInsertion() {
		VariantDataCorrector corr = new VariantDataCorrector("CGAT", "C", 100);
		Assert.assertEquals(corr.getPosition(), 101);
		Assert.assertEquals(corr.getRef(), "GAT");
		Assert.assertEquals(corr.getAlt(), "");
	}

	/** Test with substitution data. */
	@Test
	public void testSubstitution() {
		VariantDataCorrector corr = new VariantDataCorrector("CCGA", "CGAT", 100);
		Assert.assertEquals(corr.getPosition(), 101);
		Assert.assertEquals(corr.getRef(), "CGA");
		Assert.assertEquals(corr.getAlt(), "GAT");
	}

	/** Test with deletion data. */
	@Test
	public void testDeletion() {
		VariantDataCorrector corr = new VariantDataCorrector("C", "CGAT", 100);
		Assert.assertEquals(corr.getPosition(), 101);
		Assert.assertEquals(corr.getRef(), "");
		Assert.assertEquals(corr.getAlt(), "GAT");
	}

}
