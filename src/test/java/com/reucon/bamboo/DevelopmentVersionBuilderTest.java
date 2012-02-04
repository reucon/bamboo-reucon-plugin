package com.reucon.bamboo;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class DevelopmentVersionBuilderTest
{
    private DevelopmentVersionBuilder builder;

    @Before
    public void setUp() throws Exception
    {
        this.builder = new DevelopmentVersionBuilder();
    }

    @Test
    public void testBuildDevelopmentVersionWithReleaseCandidate() throws Exception
    {
        final String developmentVersion = builder.buildDevelopmentVersion("1.2.3.RC1");
        assertEquals("1.2.3.CI-SNAPSHOT", developmentVersion);
    }

    @Test
    public void testBuildDevelopmentVersionWithRelease() throws Exception
    {
        final String developmentVersion = builder.buildDevelopmentVersion("1.2.3.RELEASE");
        assertEquals("1.2.4.CI-SNAPSHOT", developmentVersion);
    }

    @Test
    public void testBuildDevelopmentVersionWithoutClassifier() throws Exception
    {
        final String developmentVersion = builder.buildDevelopmentVersion("1.2.3");
        assertEquals("1.2.4.CI-SNAPSHOT", developmentVersion);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildDevelopmentVersionWithEmptyClassifier() throws Exception
    {
        builder.buildDevelopmentVersion("1.2.3.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildDevelopmentVersionWithInvalidReleaseVersion() throws Exception
    {
        builder.buildDevelopmentVersion("1.2.3.RC1 (Early Q1)");
    }
}
