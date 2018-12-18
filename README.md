# gitlab-release-maven-plugin

[![Coverage Status](https://coveralls.io/repos/avides/gitlab-release-maven-plugin/badge.svg)](https://coveralls.io/r/avides/gitlab-release-maven-plugin)
[![Build Status](https://travis-ci.org/avides/gitlab-release-maven-plugin.svg?branch=master)](https://travis-ci.org/avides/gitlab-release-maven-plugin)

The gitlab-release-maven-plugin creates tags on your GitLab Repository by your project version in your maven build.

## Plugin

### With required configuration options
```
<plugin>
    <groupId>com.avides.gitlab</groupId>
    <artifactId>gitlab-release-maven-plugin</artifactId>
    <version>2.2.0-RELEASE</version>
</plugin>
```

### With all options
```
<plugin>
    <groupId>com.avides.gitlab</groupId>
    <artifactId>gitlab-release-maven-plugin</artifactId>
    <version>2.2.0-RELEASE</version>
    <configuration>
        <gitlabHost>http://your-custom-gitlab-domain/</gitlabHost>
        <gitlabAccessToken>GITLAB_ACCESS_TOKEN</gitlabAccessToken>
        <gitlabRepositoryNamespace>repository-namespace</gitlabRepositoryNamespace>
        <gitlabRepositoryName>project-name</gitlabRepositoryName>
        <projectVersion>0.1.0-RELEASE</projectVersion>
        <gitlabPreReleaseDesired>true</gitlabPreReleaseDesired>
        <gitlabBranchName>YOUR_DEPLOYMENT_BRANCH</gitlabBranchName>        
    </configuration>
</plugin>
```

## Configuration options
| Property | Description | Required |
| --- | ----------- | ---- |
| gitlabHost | Your custom GitLab URL (default: `https://gitlab.com`) | No |
| gitlabAccessToken | Your User GitLab Access Token (Pass this token directly in the execute command, `-DgitlabAccessToken=ACCESS_TOKEN`) | Yes |
| gitlabRepositoryNamespace | Repository namespace (If `scm` is set, it will be resolved automatically) | No |
| gitlabRepositoryName | Repository name (default: `${project.name}`) | No |
| projectVersion | Project version and finally the Release-Tag name (default: `${project.version}`) | No |
| gitlabPreReleaseDesired | If `true`, PRE-Releases will be create a Release-Tag, otherwise not (default: `false`) | No |
| gitlabBranchName | Your deployment branch (default: `master`) | No |

## Usage
```
mvn gitlab-release:release -DgitlabAccessToken=ACCESS_TOKEN
```
