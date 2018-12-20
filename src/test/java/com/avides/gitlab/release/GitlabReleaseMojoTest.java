package com.avides.gitlab.release;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;

import org.apache.maven.plugin.MojoExecutionException;
import org.easymock.TestSubject;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.http.GitlabHTTPRequestor;
import org.gitlab.api.models.GitlabBranchCommit;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabTag;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.MockStrict;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GitlabAPI.class)
public class GitlabReleaseMojoTest
{
    private static final String GITLAB_HOST = "GITLAB_HOST";

    private static final String GITLAB_ACCESS_TOKEN = "GITLAB_ACCESS_TOKEN";

    private static final String REPOSITORY_NAMESPACE = "REPOSITORY_NAMESPACE";

    private static final String REPOSITORY_NAME = "REPOSITORY_NAME";

    private static final String PROJECT_VERSION = "1.0.0-RELEASE";

    @TestSubject
    private final GitlabReleaseMojo gitlabReleaseMojo = new GitlabReleaseMojo();

    @MockStrict
    private GitlabAPI gitlabAPI;

    @MockStrict
    private GitlabProject gitlabProject;

    @MockStrict
    private GitlabCommit gitlabCommit;

    @MockStrict
    private GitlabCommit mergeBranchCommit;

    @MockStrict
    private GitlabCommit anotherCommit;

    @MockStrict
    private GitlabTag gitlabTag;

    @MockStrict
    private GitlabBranchCommit gitlabTagBranchCommit;

    @MockStrict
    private GitlabTag addedGitlabTag;

    @MockStrict
    private GitlabHTTPRequestor gitlabHTTPRequestor;

    @Before
    public void setUp()
    {
        gitlabReleaseMojo.setGitlabHost(GITLAB_HOST);
        gitlabReleaseMojo.setGitlabAccessToken(GITLAB_ACCESS_TOKEN);
        gitlabReleaseMojo.setGitlabRepositoryNamespace(REPOSITORY_NAMESPACE);
        gitlabReleaseMojo.setGitlabRepositoryName(REPOSITORY_NAME);
        gitlabReleaseMojo.setProjectVersion(PROJECT_VERSION);
    }

    @Test
    public void testExecute() throws Exception
    {
        mockStatic(GitlabAPI.class);
        GitlabAPI.connect(GITLAB_HOST, GITLAB_ACCESS_TOKEN);
        expectLastCall().andReturn(gitlabAPI);

        gitlabAPI.getHost();
        expectLastCall().andReturn(GITLAB_HOST);

        gitlabAPI.getProject(REPOSITORY_NAMESPACE, REPOSITORY_NAME);
        expectLastCall().andReturn(gitlabProject);

        gitlabProject.getNameWithNamespace();
        expectLastCall().andReturn(REPOSITORY_NAMESPACE + "/" + REPOSITORY_NAME);

        gitlabProject.getId();
        expectLastCall().andReturn(Integer.valueOf(1));

        gitlabAPI.getTags(gitlabProject);
        expectLastCall().andReturn(emptyList());

        gitlabAPI.retrieve();
        expectLastCall().andReturn(gitlabHTTPRequestor);

        gitlabHTTPRequestor.to("/projects/1/repository/commits?ref_name=master", GitlabCommit[].class);
        expectLastCall().andReturn(new GitlabCommit[] { gitlabCommit });

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabCommit.getTitle();
        expectLastCall().andReturn("COMMIT_TITLE").times(2);

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabAPI.addTag(gitlabProject, PROJECT_VERSION, "COMMIT_REF", "", "* COMMIT_TITLE (COMMIT_REF)\n");
        expectLastCall().andReturn(addedGitlabTag);

        addedGitlabTag.getName();
        expectLastCall().andReturn(PROJECT_VERSION);

        replayAll();

        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithScmUrlInsteadOfGitlabRepositoryNamespaceConfigured() throws Exception
    {
        mockStatic(GitlabAPI.class);
        GitlabAPI.connect(GITLAB_HOST, GITLAB_ACCESS_TOKEN);
        expectLastCall().andReturn(gitlabAPI);

        gitlabAPI.getHost();
        expectLastCall().andReturn(GITLAB_HOST);

        gitlabAPI.getProject(REPOSITORY_NAMESPACE, REPOSITORY_NAME);
        expectLastCall().andReturn(gitlabProject);

        gitlabProject.getNameWithNamespace();
        expectLastCall().andReturn(REPOSITORY_NAMESPACE + "/" + REPOSITORY_NAME);

        gitlabProject.getId();
        expectLastCall().andReturn(Integer.valueOf(1));

        gitlabAPI.getTags(gitlabProject);
        expectLastCall().andReturn(emptyList());

        gitlabAPI.retrieve();
        expectLastCall().andReturn(gitlabHTTPRequestor);

        gitlabHTTPRequestor.to("/projects/1/repository/commits?ref_name=master", GitlabCommit[].class);
        expectLastCall().andReturn(new GitlabCommit[] { gitlabCommit });

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabCommit.getTitle();
        expectLastCall().andReturn("COMMIT_TITLE").times(2);

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabAPI.addTag(gitlabProject, PROJECT_VERSION, "COMMIT_REF", "", "* COMMIT_TITLE (COMMIT_REF)\n");
        expectLastCall().andReturn(addedGitlabTag);

        addedGitlabTag.getName();
        expectLastCall().andReturn(PROJECT_VERSION);

        replayAll();

        gitlabReleaseMojo.setGitlabRepositoryNamespace(null);
        gitlabReleaseMojo.setProjectScmUrl("GITLAB_HOST/REPOSITORY_NAMESPACE/REPOSITORY_NAME");
        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithOtherBranchName() throws Exception
    {
        mockStatic(GitlabAPI.class);
        GitlabAPI.connect(GITLAB_HOST, GITLAB_ACCESS_TOKEN);
        expectLastCall().andReturn(gitlabAPI);

        gitlabAPI.getHost();
        expectLastCall().andReturn(GITLAB_HOST);

        gitlabAPI.getProject(REPOSITORY_NAMESPACE, REPOSITORY_NAME);
        expectLastCall().andReturn(gitlabProject);

        gitlabProject.getNameWithNamespace();
        expectLastCall().andReturn(REPOSITORY_NAMESPACE + "/" + REPOSITORY_NAME);

        gitlabProject.getId();
        expectLastCall().andReturn(Integer.valueOf(1));

        gitlabAPI.getTags(gitlabProject);
        expectLastCall().andReturn(emptyList());

        gitlabAPI.retrieve();
        expectLastCall().andReturn(gitlabHTTPRequestor);

        gitlabHTTPRequestor.to("/projects/1/repository/commits?ref_name=OTHER_BRANCH_NAME", GitlabCommit[].class);
        expectLastCall().andReturn(new GitlabCommit[] { gitlabCommit });

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabCommit.getTitle();
        expectLastCall().andReturn("COMMIT_TITLE").times(2);

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabAPI.addTag(gitlabProject, PROJECT_VERSION, "COMMIT_REF", "", "* COMMIT_TITLE (COMMIT_REF)\n");
        expectLastCall().andReturn(addedGitlabTag);

        addedGitlabTag.getName();
        expectLastCall().andReturn(PROJECT_VERSION);

        replayAll();

        gitlabReleaseMojo.setGitlabBranchName("OTHER_BRANCH_NAME");
        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithMultipleCommits() throws Exception
    {
        mockStatic(GitlabAPI.class);
        GitlabAPI.connect(GITLAB_HOST, GITLAB_ACCESS_TOKEN);
        expectLastCall().andReturn(gitlabAPI);

        gitlabAPI.getHost();
        expectLastCall().andReturn(GITLAB_HOST);

        gitlabAPI.getProject(REPOSITORY_NAMESPACE, REPOSITORY_NAME);
        expectLastCall().andReturn(gitlabProject);

        gitlabProject.getNameWithNamespace();
        expectLastCall().andReturn(REPOSITORY_NAMESPACE + "/" + REPOSITORY_NAME);

        gitlabProject.getId();
        expectLastCall().andReturn(Integer.valueOf(1));

        gitlabAPI.getTags(gitlabProject);
        expectLastCall().andReturn(emptyList());

        gitlabAPI.retrieve();
        expectLastCall().andReturn(gitlabHTTPRequestor);

        gitlabHTTPRequestor.to("/projects/1/repository/commits?ref_name=master", GitlabCommit[].class);
        expectLastCall().andReturn(new GitlabCommit[] { gitlabCommit, mergeBranchCommit, anotherCommit });

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabCommit.getTitle();
        expectLastCall().andReturn("COMMIT_TITLE").times(2);

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        mergeBranchCommit.getTitle();
        expectLastCall().andReturn("Merge branch_COMMIT_TITLE");

        anotherCommit.getTitle();
        expectLastCall().andReturn("ANOTHER_COMMIT_TITLE").times(2);

        anotherCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabAPI.addTag(gitlabProject, PROJECT_VERSION, "COMMIT_REF", "", "* COMMIT_TITLE (COMMIT_REF)\n* ANOTHER_COMMIT_TITLE (COMMIT_REF)\n");
        expectLastCall().andReturn(addedGitlabTag);

        addedGitlabTag.getName();
        expectLastCall().andReturn(PROJECT_VERSION);

        replayAll();

        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithPreviousReleases() throws Exception
    {
        mockStatic(GitlabAPI.class);
        GitlabAPI.connect(GITLAB_HOST, GITLAB_ACCESS_TOKEN);
        expectLastCall().andReturn(gitlabAPI);

        gitlabAPI.getHost();
        expectLastCall().andReturn(GITLAB_HOST);

        gitlabAPI.getProject(REPOSITORY_NAMESPACE, REPOSITORY_NAME);
        expectLastCall().andReturn(gitlabProject);

        gitlabProject.getNameWithNamespace();
        expectLastCall().andReturn(REPOSITORY_NAMESPACE + "/" + REPOSITORY_NAME);

        gitlabProject.getId();
        expectLastCall().andReturn(Integer.valueOf(1));

        gitlabAPI.getTags(gitlabProject);
        expectLastCall().andReturn(singletonList(gitlabTag));

        gitlabTag.getName();
        expectLastCall().andReturn("0.1.0-RELEASE").times(2);

        gitlabTag.getCommit();
        expectLastCall().andReturn(gitlabTagBranchCommit);

        gitlabTagBranchCommit.getCommittedDate();
        expectLastCall().andReturn(Date.from(Instant.parse("2018-10-23T21:18:30.00Z")));

        gitlabAPI.retrieve();
        expectLastCall().andReturn(gitlabHTTPRequestor);

        gitlabHTTPRequestor.to("/projects/1/repository/commits?ref_name=master&since=2018-10-23T21%3A18%3A30", GitlabCommit[].class);
        expectLastCall().andReturn(new GitlabCommit[] { gitlabCommit, anotherCommit });

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabCommit.getTitle();
        expectLastCall().andReturn("COMMIT_TITLE").times(2);

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabAPI.addTag(gitlabProject, PROJECT_VERSION, "COMMIT_REF", "", "* COMMIT_TITLE (COMMIT_REF)\n");
        expectLastCall().andReturn(addedGitlabTag);

        addedGitlabTag.getName();
        expectLastCall().andReturn(PROJECT_VERSION);

        replayAll();

        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithPreviousTagsAndNoPreviousReleases() throws Exception
    {
        mockStatic(GitlabAPI.class);
        GitlabAPI.connect(GITLAB_HOST, GITLAB_ACCESS_TOKEN);
        expectLastCall().andReturn(gitlabAPI);

        gitlabAPI.getHost();
        expectLastCall().andReturn(GITLAB_HOST);

        gitlabAPI.getProject(REPOSITORY_NAMESPACE, REPOSITORY_NAME);
        expectLastCall().andReturn(gitlabProject);

        gitlabProject.getNameWithNamespace();
        expectLastCall().andReturn(REPOSITORY_NAMESPACE + "/" + REPOSITORY_NAME);

        gitlabProject.getId();
        expectLastCall().andReturn(Integer.valueOf(1));

        gitlabAPI.getTags(gitlabProject);
        expectLastCall().andReturn(singletonList(gitlabTag));

        gitlabTag.getName();
        expectLastCall().andReturn("0.1.0-SNAPSHOT").times(2);

        gitlabAPI.retrieve();
        expectLastCall().andReturn(gitlabHTTPRequestor);

        gitlabHTTPRequestor.to("/projects/1/repository/commits?ref_name=master", GitlabCommit[].class);
        expectLastCall().andReturn(new GitlabCommit[] { gitlabCommit });

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabCommit.getTitle();
        expectLastCall().andReturn("COMMIT_TITLE").times(2);

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabAPI.addTag(gitlabProject, PROJECT_VERSION, "COMMIT_REF", "", "* COMMIT_TITLE (COMMIT_REF)\n");
        expectLastCall().andReturn(addedGitlabTag);

        addedGitlabTag.getName();
        expectLastCall().andReturn(PROJECT_VERSION);

        replayAll();

        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithoutProjectVersionIsSnapshot() throws Exception
    {
        replayAll();

        gitlabReleaseMojo.setProjectVersion("1.0.0-SNAPSHOT");
        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithoutProjectVersionIsAlpha() throws Exception
    {
        replayAll();

        gitlabReleaseMojo.setProjectVersion("1.0.0-alpha");
        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithoutProjectVersionIsBeta() throws Exception
    {
        replayAll();

        gitlabReleaseMojo.setProjectVersion("1.0.0-beta");
        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithoutProjectVersionIsReleaseCandidate1() throws Exception
    {
        replayAll();

        gitlabReleaseMojo.setProjectVersion("1.0.0-RC1");
        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithoutProjectVersionIsMilestone1() throws Exception
    {
        replayAll();

        gitlabReleaseMojo.setProjectVersion("1.0.0-M1");
        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithoutProjectVersionIsBuildSnapshot() throws Exception
    {
        replayAll();

        gitlabReleaseMojo.setProjectVersion("1.0.0-BUILD_SNAPSHOT");
        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithProjectVersionIsNotReleaseAndGitlabPreReleaseDesiredIsTrue() throws Exception
    {
        mockStatic(GitlabAPI.class);
        GitlabAPI.connect(GITLAB_HOST, GITLAB_ACCESS_TOKEN);
        expectLastCall().andReturn(gitlabAPI);

        gitlabAPI.getHost();
        expectLastCall().andReturn(GITLAB_HOST);

        gitlabAPI.getProject(REPOSITORY_NAMESPACE, REPOSITORY_NAME);
        expectLastCall().andReturn(gitlabProject);

        gitlabProject.getNameWithNamespace();
        expectLastCall().andReturn(REPOSITORY_NAMESPACE + "/" + REPOSITORY_NAME);

        gitlabProject.getId();
        expectLastCall().andReturn(Integer.valueOf(1));

        gitlabAPI.getTags(gitlabProject);
        expectLastCall().andReturn(emptyList());

        gitlabAPI.retrieve();
        expectLastCall().andReturn(gitlabHTTPRequestor);

        gitlabHTTPRequestor.to("/projects/1/repository/commits?ref_name=master", GitlabCommit[].class);
        expectLastCall().andReturn(new GitlabCommit[] { gitlabCommit });

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabCommit.getTitle();
        expectLastCall().andReturn("COMMIT_TITLE").times(2);

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabAPI.addTag(gitlabProject, "1.0.0-SNAPSHOT", "COMMIT_REF", "", "* COMMIT_TITLE (COMMIT_REF)\n");
        expectLastCall().andReturn(addedGitlabTag);

        addedGitlabTag.getName();
        expectLastCall().andReturn("1.0.0-SNAPSHOT");

        replayAll();

        gitlabReleaseMojo.setProjectVersion("1.0.0-SNAPSHOT");
        gitlabReleaseMojo.setGitlabPreReleaseDesired(true);
        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithTagAlreadyExists() throws Exception
    {
        mockStatic(GitlabAPI.class);
        GitlabAPI.connect(GITLAB_HOST, GITLAB_ACCESS_TOKEN);
        expectLastCall().andReturn(gitlabAPI);

        gitlabAPI.getHost();
        expectLastCall().andReturn(GITLAB_HOST);

        gitlabAPI.getProject(REPOSITORY_NAMESPACE, REPOSITORY_NAME);
        expectLastCall().andReturn(gitlabProject);

        gitlabProject.getNameWithNamespace();
        expectLastCall().andReturn(REPOSITORY_NAMESPACE + "/" + REPOSITORY_NAME);

        gitlabAPI.getTags(gitlabProject);
        expectLastCall().andReturn(singletonList(gitlabTag));

        gitlabTag.getName();
        expectLastCall().andReturn(PROJECT_VERSION);

        replayAll();

        gitlabReleaseMojo.execute();

        verifyAll();
    }

    @Test
    public void testExecuteWithExceptionOnResolveProject() throws Exception
    {
        mockStatic(GitlabAPI.class);
        GitlabAPI.connect(GITLAB_HOST, GITLAB_ACCESS_TOKEN);
        expectLastCall().andReturn(gitlabAPI);

        gitlabAPI.getHost();
        expectLastCall().andReturn(GITLAB_HOST);

        gitlabAPI.getProject(REPOSITORY_NAMESPACE, REPOSITORY_NAME);
        expectLastCall().andThrow(new IOException());

        replayAll();

        assertThatThrownBy(gitlabReleaseMojo::execute).isInstanceOf(MojoExecutionException.class).hasMessage("Failed to resolve project");

        verifyAll();
    }

    @Test
    public void testExecuteWithExceptionOnResolveLatestCommitOnBranch() throws Exception
    {
        mockStatic(GitlabAPI.class);
        GitlabAPI.connect(GITLAB_HOST, GITLAB_ACCESS_TOKEN);
        expectLastCall().andReturn(gitlabAPI);

        gitlabAPI.getHost();
        expectLastCall().andReturn(GITLAB_HOST);

        gitlabAPI.getProject(REPOSITORY_NAMESPACE, REPOSITORY_NAME);
        expectLastCall().andReturn(gitlabProject);

        gitlabProject.getNameWithNamespace();
        expectLastCall().andReturn(REPOSITORY_NAMESPACE + "/" + REPOSITORY_NAME);

        gitlabProject.getId();
        expectLastCall().andReturn(Integer.valueOf(1));

        gitlabAPI.getTags(gitlabProject);
        expectLastCall().andReturn(emptyList());

        gitlabAPI.retrieve();
        expectLastCall().andReturn(gitlabHTTPRequestor);

        gitlabHTTPRequestor.to("/projects/1/repository/commits?ref_name=master", GitlabCommit[].class);
        expectLastCall().andThrow(new IOException());

        replayAll();

        assertThatThrownBy(gitlabReleaseMojo::execute).isInstanceOf(MojoExecutionException.class).hasMessage("Failed to resolve latest commit");

        verifyAll();
    }

    @Test
    public void testExecuteWithExceptionOnAddTag() throws Exception
    {
        mockStatic(GitlabAPI.class);
        GitlabAPI.connect(GITLAB_HOST, GITLAB_ACCESS_TOKEN);
        expectLastCall().andReturn(gitlabAPI);

        gitlabAPI.getHost();
        expectLastCall().andReturn(GITLAB_HOST);

        gitlabAPI.getProject(REPOSITORY_NAMESPACE, REPOSITORY_NAME);
        expectLastCall().andReturn(gitlabProject);

        gitlabProject.getNameWithNamespace();
        expectLastCall().andReturn(REPOSITORY_NAMESPACE + "/" + REPOSITORY_NAME);

        gitlabProject.getId();
        expectLastCall().andReturn(Integer.valueOf(1));

        gitlabAPI.getTags(gitlabProject);
        expectLastCall().andReturn(emptyList());

        gitlabAPI.retrieve();
        expectLastCall().andReturn(gitlabHTTPRequestor);

        gitlabHTTPRequestor.to("/projects/1/repository/commits?ref_name=master", GitlabCommit[].class);
        expectLastCall().andReturn(new GitlabCommit[] { gitlabCommit });

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabCommit.getTitle();
        expectLastCall().andReturn("COMMIT_TITLE").times(2);

        gitlabCommit.getId();
        expectLastCall().andReturn("COMMIT_REF");

        gitlabAPI.addTag(gitlabProject, PROJECT_VERSION, "COMMIT_REF", "", "* COMMIT_TITLE (COMMIT_REF)\n");
        expectLastCall().andThrow(new IOException());

        replayAll();

        assertThatThrownBy(gitlabReleaseMojo::execute).isInstanceOf(MojoExecutionException.class).hasMessage("Failed to add tag");

        verifyAll();
    }

    @Test
    public void testExecuteWithoutScmUrlAndGitlabRepositoryNamespace() throws Exception
    {
        replayAll();

        gitlabReleaseMojo.setGitlabRepositoryNamespace(null);
        gitlabReleaseMojo.setProjectScmUrl(null);
        gitlabReleaseMojo.execute();

        verifyAll();
    }
}
