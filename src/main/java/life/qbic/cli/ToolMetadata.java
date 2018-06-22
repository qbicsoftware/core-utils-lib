package life.qbic.cli;

import org.apache.commons.lang3.Validate;

/**
 * Simple struct-like class to hold information about tools.
 */
public class ToolMetadata {

    private final String toolName;
    private final String toolVersion;
    private final String toolRepoUrl;

    /**
     * @param toolName the name of the tool.
     * @param toolVersion the version of the tool.
     * @param toolRepoUrl the repository URL.
     */
    public ToolMetadata(final String toolName, final String toolVersion, final String toolRepoUrl) {
        Validate.notBlank(toolName, "toolName is required and cannot be empty, null or contain only whitespaces");
        Validate.notBlank(toolVersion, "toolVersion is required and cannot be empty, null or contain only whitespaces");
        Validate.notBlank(toolRepoUrl, "toolRepoUrl is required and cannot be empty, null or contain only whitespaces");
        this.toolName = toolName;
        this.toolVersion = toolVersion;
        this.toolRepoUrl = toolRepoUrl;
    }

    /**
     * @return the name of the tool.
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * @return the version of the tool.
     */
    public String getToolVersion() {
        return toolVersion;
    }

    /**
     * @return the URL of the remote repository.
     */
    public String getToolRepoUrl() {
        return toolRepoUrl;
    }
}
