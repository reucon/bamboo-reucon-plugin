package com.reucon.bamboo;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.opensymphony.xwork.TextProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CreateReleasePropertiesTaskConfigurator extends AbstractTaskConfigurator
{
    public static final String RELEASE_VERSION_VARIABLE = "releaseVersionVariable";
    public static final String RELEASE_QUALIFIER = "releaseQualifier";
    public static final String SNAPSHOT_QUALIFIER = "snapshotQualifier";

    private static final String DEFAULT_RELEASE_VERSION_VARIABLE = "jira.version";
    private static final String DEFAULT_RELEASE_QUALIFIER = "RELEASE";
    private static final String DEFAULT_SNAPSHOT_QUALIFIER = "CI-SNAPSHOT";

    private TextProvider textProvider;

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(RELEASE_VERSION_VARIABLE, params.getString(RELEASE_VERSION_VARIABLE));
        config.put(RELEASE_QUALIFIER, params.getString(RELEASE_QUALIFIER));
        config.put(SNAPSHOT_QUALIFIER, params.getString(SNAPSHOT_QUALIFIER));
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
        context.put(RELEASE_VERSION_VARIABLE, DEFAULT_RELEASE_VERSION_VARIABLE);
        context.put(RELEASE_QUALIFIER, DEFAULT_RELEASE_QUALIFIER);
        context.put(SNAPSHOT_QUALIFIER, DEFAULT_SNAPSHOT_QUALIFIER);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);
        populateContextForEditOrView(context, taskDefinition);
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForView(context, taskDefinition);
        populateContextForEditOrView(context, taskDefinition);
    }

    public void populateContextForEditOrView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        context.put(RELEASE_VERSION_VARIABLE, taskDefinition.getConfiguration().get(RELEASE_VERSION_VARIABLE));
        context.put(RELEASE_QUALIFIER, taskDefinition.getConfiguration().get(RELEASE_QUALIFIER));
        context.put(SNAPSHOT_QUALIFIER, taskDefinition.getConfiguration().get(SNAPSHOT_QUALIFIER));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
    {
        super.validate(params, errorCollection);

        validateNotEmpty(params, errorCollection, RELEASE_VERSION_VARIABLE);
        validateNotEmpty(params, errorCollection, RELEASE_QUALIFIER);
        validateNotEmpty(params, errorCollection, SNAPSHOT_QUALIFIER);
    }

    private void validateNotEmpty(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key)
    {
        final String value = params.getString(key);
        if (StringUtils.isEmpty(value))
        {
            errorCollection.addError(key, textProvider.getText("com.reucon.bamboo." + key + ".error"));
        }
    }

    public void setTextProvider(final TextProvider textProvider)
    {
        this.textProvider = textProvider;
    }
}
