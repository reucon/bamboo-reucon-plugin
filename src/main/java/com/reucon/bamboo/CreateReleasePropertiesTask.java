package com.reucon.bamboo;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static com.reucon.bamboo.CreateReleasePropertiesTaskConfigurator.RELEASE_QUALIFIER;
import static com.reucon.bamboo.CreateReleasePropertiesTaskConfigurator.RELEASE_VERSION_VARIABLE;
import static com.reucon.bamboo.CreateReleasePropertiesTaskConfigurator.SNAPSHOT_QUALIFIER;

public class CreateReleasePropertiesTask implements TaskType
{
    private static final String RELEASE_PROPERTIES_FILENAME = "release.properties";
    private static final String RELEASE_VERSION_PROPERTY = "releaseVersion";
    private static final String DEVELOPMENT_VERSION_PROPERTY = "developmentVersion";

    private CustomVariableContext customVariableContext;

    public void setCustomVariableContext(CustomVariableContext customVariableContext)
    {
        this.customVariableContext = customVariableContext;
    }

    @NotNull
    @java.lang.Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException
    {
        final TaskResultBuilder taskResultBuilder = TaskResultBuilder.create(taskContext);

        final BuildLogger buildLogger = taskContext.getBuildLogger();
        final BuildContext buildContext = taskContext.getBuildContext();
        final VariableContext variableContext = buildContext.getVariableContext();
        final Map<String, VariableDefinitionContext> definitions = variableContext.getDefinitions();

        final String releaseVersionVariableName = taskContext.getConfigurationMap().get(RELEASE_VERSION_VARIABLE);
        final VariableDefinitionContext releaseVersionVariable = definitions.get(releaseVersionVariableName);
        if (releaseVersionVariable == null)
        {
            buildLogger.addErrorLogEntry("Unable to read releaseVersion from variable '" + releaseVersionVariableName + "'");
            return taskResultBuilder.failed().build();
        }
        final String releaseVersion = releaseVersionVariable.getValue();

        final DevelopmentVersionBuilder developmentVersionBuilder = new DevelopmentVersionBuilder();
        developmentVersionBuilder.setReleaseClassifier(taskContext.getConfigurationMap().get(RELEASE_QUALIFIER));
        developmentVersionBuilder.setSnapshotClassifier(taskContext.getConfigurationMap().get(SNAPSHOT_QUALIFIER));
        final String developmentVersion;
        try
        {
            developmentVersion = developmentVersionBuilder.buildDevelopmentVersion(releaseVersion);
        }
        catch (IllegalArgumentException e)
        {
            buildLogger.addErrorLogEntry(String.format("Detected invalid releaseVersion '%s': %s", releaseVersion, e.getMessage()));
            return taskResultBuilder.failed().build();
        }

        buildLogger.addBuildLogEntry(String.format("Detected releaseVersion '%s' and derived developmentVersion '%s'",
                releaseVersion, developmentVersion));

        setVariable(taskContext, DEVELOPMENT_VERSION_PROPERTY, developmentVersion);
        setVariable(taskContext, RELEASE_VERSION_PROPERTY, releaseVersion);

        return taskResultBuilder.success().build();
    }

    private void setVariable(TaskContext taskContext, String key, String value)
    {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        final VariableContext variableContext = taskContext.getBuildContext().getVariableContext();
        final Map<String, VariableDefinitionContext> definitions = variableContext.getDefinitions();

        final VariableDefinitionContext definition = definitions.get(key);
        if (definition != null)
        {
            if (StringUtils.isEmpty(definition.getValue()))
            {
                definition.setValue(value);
                buildLogger.addBuildLogEntry(String.format("Set variable %s=%s", key, value));
            }
            else
            {
                buildLogger.addBuildLogEntry(String.format("Not setting variable %s because it is already set", key));
            }
            return;
        }
        customVariableContext.addCustomData(key, value);
        buildLogger.addBuildLogEntry(String.format("Set custom variable %s=%s", key, value));
    }
}