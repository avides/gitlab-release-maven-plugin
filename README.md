# gitlab-release-maven-plugin

[![Maven Central](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/avides/gitlab/gitlab-release-maven-plugin/maven-metadata.xml.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.avides.gitlab%22%20AND%20a%3A%22gitlab-release-maven-plugin%22)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/d7eca0c7e4684857ab166dcbcf2b75b5)](https://www.codacy.com/app/avides-builds/gitlab-release-maven-plugin)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/d7eca0c7e4684857ab166dcbcf2b75b5)](https://www.codacy.com/app/avides-builds/gitlab-release-maven-plugin)
[![Build Status](https://travis-ci.org/avides/gitlab-release-maven-plugin.svg?branch=master)](https://travis-ci.org/avides/gitlab-release-maven-plugin)

The gitlab-release-maven-plugin creates tags on your GitLab Repository by your project version in your maven build.

## Plugin

### With required configuration options
```xml
<plugin>
    <groupId>com.avides.gitlab</groupId>
    <artifactId>gitlab-release-maven-plugin</artifactId>
    <version>2.2.0-RELEASE</version>
</plugin>
```

### With all options
```xml
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
| Property                  | Description                                                                                                         | Required |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------- | -------- |
| gitlabHost                | Your custom GitLab URL (default: `https://gitlab.com`)                                                              | No       |
| gitlabAccessToken         | Your User GitLab Access Token (Pass this token directly in the execute command, `-DgitlabAccessToken=ACCESS_TOKEN`) | Yes      |
| gitlabRepositoryNamespace | Repository namespace (If `project.scm.url` is set in your pom, it will be resolved automatically.)                  | No       |
| gitlabRepositoryName      | Repository name (default: `${project.name}`)                                                                        | No       |
| projectVersion            | Project version and finally the Release-Tag name (default: `${project.version}`)                                    | No       |
| gitlabPreReleaseDesired   | If `true`, PRE-Releases will be create a Release-Tag, otherwise not (default: `false`)                              | No       |
| gitlabBranchName          | Your deployment branch (default: `master`)                                                                          | No       |

## Usage
```bash
mvn gitlab-release:release -DgitlabAccessToken=ACCESS_TOKEN
```
