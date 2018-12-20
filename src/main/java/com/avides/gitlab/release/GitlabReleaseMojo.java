package com.avides.gitlab.release;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.http.Query;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabTag;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * GitLab release maven plugin is used to create a release tag in the GitLab repository.
 * <p>
 * You have to configure the <code>scm</code> or define the <code>gitlabRepositoryNamespace</code> in your pom.
 * <p>
 * If your project name is not equal to the repository name you have to define the <code>gitlabRepositoryName</code> in your pom.
 * <p>
 * The release tag description is the commit history from the last commit until the last created release tag (no pre release tags e.g 0.1.0-SNAPSHOT).
 */
@Mojo(name = "release")
@Getter
@Setter
public class GitlabReleaseMojo extends AbstractMojo
{
    private static final List<String> PRE_RELEASE_INDICATOR = asList("SNAPSHOT", "ALPHA", "BETA", "RC", "M", "BUILD_SNAPSHOT");

    @Parameter(defaultValue = "https://gitlab.com")
    private String gitlabHost;

    @Parameter(defaultValue = "${gitlabAccessToken}")
    private String gitlabAccessToken;

    @Parameter
    private String gitlabRepositoryNamespace;

    @Parameter(property = "project.scm.url")
    private String projectScmUrl;

    @Parameter(property = "project.name")
    private String gitlabRepositoryName;

    @Parameter(property = "project.version")
    private String projectVersion;

    @Parameter(defaultValue = "false")
    private boolean gitlabPreReleaseDesired;

    @Parameter(defaultValue = "${gitlabBranchName}")
    private String gitlabBranchName;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private GitlabAPI gitlabAPI;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private List<GitlabCommit> lastCommits;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private List<GitlabTag> lastTags;

    /**
     * Execute the gitlab release maven plugin.
     *
     * @throws MojoExecutionException If something went wrong
     */
    public void execute() throws MojoExecutionException
    {
        if (canResolveGitlabRepositoryNamespace())
        {
            resolveGitlabSourceBranch();
            release();
        }
        else
        {
            getLog().warn("Gitlab repository namespace not found -> Please define 'scm.url' or 'gitlabRepositoryNamespace' in your POM.");
        }
    }

    private boolean canResolveGitlabRepositoryNamespace()
    {
        return StringUtils.isNotBlank(gitlabRepositoryNamespace) || StringUtils.isNotBlank(projectScmUrl);
    }

    private void resolveGitlabSourceBranch()
    {
        if (StringUtils.isBlank(gitlabBranchName))
        {
            getLog().info("Using branch 'master' as default");
            gitlabBranchName = "master";
        }
    }

    private void release() throws MojoExecutionException
    {
        if (gitlabPreReleaseDesired || !isPreRelease())
        {
            resolveGitlabRepositoryNamespace();
            connectToGitlab();
            createReleaseTag();
        }
        else
        {
            getLog().info("Don't add new tag for a pre-release: " + projectVersion);
        }
    }

    private void resolveGitlabRepositoryNamespace()
    {
        if (StringUtils.isBlank(gitlabRepositoryNamespace) && StringUtils.isNotBlank(projectScmUrl))
        {
            gitlabRepositoryNamespace = projectScmUrl.replace(gitlabHost, "");
            gitlabRepositoryNamespace = gitlabRepositoryNamespace.startsWith("/") ? gitlabRepositoryNamespace.replaceFirst("/", "") : gitlabRepositoryNamespace;
            gitlabRepositoryNamespace = gitlabRepositoryNamespace.substring(0, gitlabRepositoryNamespace.indexOf('/'));
            getLog().info("Resolved namespace: " + gitlabRepositoryNamespace);
        }
    }

    private void connectToGitlab()
    {
        getLog().info("Connecting to gitlab...");
        gitlabAPI = GitlabAPI.connect(gitlabHost, gitlabAccessToken);
        getLog().info("Connected to gitlab: " + gitlabAPI.getHost());
    }

    private GitlabProject resolveProject() throws MojoExecutionException
    {
        try
        {
            getLog().info("Resolving repository...");
            GitlabProject project = gitlabAPI.getProject(gitlabRepositoryNamespace, gitlabRepositoryName);
            getLog().info("Resolved repository: " + project.getNameWithNamespace());
            return project;
        }
        catch (IOException e)
        {
            getLog().error("Failed to resolve project", e);
            throw new MojoExecutionException("Failed to resolve project", e);
        }
    }

    private GitlabCommit resolveLatestCommitOnBranch(Integer projectId, String since) throws MojoExecutionException
    {
        try
        {
            getLog().info("Resolving latest commits on " + gitlabBranchName + "...");

            lastCommits = new ArrayList<>(asList(gitlabAPI.retrieve().to(buildLatestCommitOnBranchUrl(projectId, since), GitlabCommit[].class)));

            // remove the commit of the last release if necessary
            if (since != null && lastCommits.size() > 1)
            {
                lastCommits.remove(lastCommits.size() - 1);
            }

            getLog().info("Resolved latest commits on " + gitlabBranchName);

            return lastCommits.get(0);
        }
        catch (IOException e)
        {
            getLog().error("Failed to resolve latest commit", e);
            throw new MojoExecutionException("Failed to resolve latest commit", e);
        }
    }

    private String buildLatestCommitOnBranchUrl(Integer projectId, String since) throws UnsupportedEncodingException
    {
        Query query = new Query();
        query.append("ref_name", gitlabBranchName);

        if (since != null)
        {
            query.append("since", since);
        }

        return GitlabProject.URL + "/" + projectId + "/repository" + GitlabCommit.URL + query;
    }

    private void createReleaseTag() throws MojoExecutionException
    {
        GitlabProject project = resolveProject();

        if (!isTagForProjectVersionAlreadyExists(project))
        {
            addTag(project, resolveLatestCommitOnBranch(project.getId(), getLastReleaseTagCreated()));
        }
        else
        {
            getLog().info("Tag already exists for version: " + projectVersion);
        }
    }

    private String getLastReleaseTagCreated()
    {
        GitlabTag gitlabTag = lastTags.stream().filter(tag ->
        {
            String tagName = tag.getName().toUpperCase();
            return PRE_RELEASE_INDICATOR.stream().noneMatch(tagName::endsWith);
        }).findFirst().orElse(null);
        return gitlabTag != null ? toLocalDateTime(gitlabTag.getCommit().getCommittedDate()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    private boolean isTagForProjectVersionAlreadyExists(GitlabProject project)
    {
        lastTags = gitlabAPI.getTags(project);
        return lastTags.stream().anyMatch(tag -> projectVersion.equals(tag.getName()));
    }

    private boolean isPreRelease()
    {
        return PRE_RELEASE_INDICATOR.stream().anyMatch(indicator -> projectVersion.toUpperCase().contains(indicator));
    }

    private void addTag(GitlabProject project, GitlabCommit commit) throws MojoExecutionException
    {
        try
        {
            getLog().info("Adding tag...");
            GitlabTag gitlabTag = gitlabAPI.addTag(project, projectVersion, commit.getId(), "", getReleaseNote());
            getLog().info("Added tag: " + gitlabTag.getName());
        }
        catch (IOException e)
        {
            getLog().error("Failed to add tag", e);
            throw new MojoExecutionException("Failed to add tag", e);
        }
    }

    private String getReleaseNote()
    {
        StringBuilder builder = new StringBuilder();
        lastCommits.stream()
                .filter(commit -> !commit.getTitle().startsWith("Merge branch"))
                .forEach(commit -> builder.append("* ").append(commit.getTitle()).append(" (").append(commit.getId()).append(")").append("\n"));
        return builder.toString();
    }

    private static LocalDateTime toLocalDateTime(Date date)
    {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
    }
}
