package com.reucon.bamboo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Determines the next development version based on the current release version.
 */
public class DevelopmentVersionBuilder
{
    private static final Pattern OSGI_VERSION_PATTERN = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)(\\.([0-9A-Za-z_-]+))?");
    private String releaseClassifier = "RELEASE";
    private String snapshotClassifier = "CI-SNAPSHOT";

    public void setReleaseClassifier(String releaseClassifier)
    {
        this.releaseClassifier = releaseClassifier;
    }

    public void setSnapshotClassifier(String snapshotClassifier)
    {
        this.snapshotClassifier = snapshotClassifier;
    }

    public String buildDevelopmentVersion(String releaseVersion) throws IllegalArgumentException
    {
        if (releaseVersion == null)
        {
            throw new IllegalArgumentException("Invalid release version: null");
        }

        final Matcher matcher = OSGI_VERSION_PATTERN.matcher(releaseVersion);

        if (matcher.matches())
        {
            int major = Integer.valueOf(matcher.group(1));
            int minor = Integer.valueOf(matcher.group(2));
            int micro = Integer.valueOf(matcher.group(3));
            String classifier = matcher.group(5);

            if (classifier == null || releaseClassifier.equals(classifier))
            {
                micro++;
            }
            return buildOsgiVersion(major, minor, micro, snapshotClassifier);
        }
        else
        {
            throw new IllegalArgumentException(String.format("Invalid release version: '%s' is not OSGI compliant.", releaseVersion));
        }
    }

    String buildOsgiVersion(Integer major, Integer minor, Integer micro, String qualifier)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(major != null ? major : "0");
        sb.append('.');
        sb.append(minor != null ? minor : "0");
        sb.append('.');
        sb.append(micro != null ? micro : "0");
        if (qualifier != null)
        {
            sb.append('.');
            sb.append(qualifier);
        }
        return sb.toString();
    }
}
