package githubservice;

import org.kohsuke.github.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GitHubService {
    public static GitHub buildServiceViaLibrary(String accessToken) throws IOException {
        return new GitHubBuilder().withOAuthToken(accessToken).build();
    }

    public static void main(String[] args) {
        try {
            // TODO: A bunch of strings to set to test
            // GitHub, repo, files stuff
            String accessToken = "";
            String refBase = "refs/heads/";
            String repoName = "mau-synopsys/test";
            String fileName = "build.gradle";
            // New change stuff
            String newBranchName = "new-branch-2";
            String newCommitMessage = "Commit 2";
            String newTitle = "PR title 2";

            // Dependency updates
            String dependency = "com.google.code.gson";
            String currentVersion = "gson:2.9.1";   // Assumes versions are given. Make sure this matches for testing
            String newVersion = "gson:2.9.0";

            // Get repo
            GitHub gitHub = buildServiceViaLibrary(accessToken);
            GHRepository testRepository = gitHub.getRepository(repoName);

            // Branch off default, modify file, and commit
            String defaultBranch = testRepository.getDefaultBranch();
            String defaultBranchSha = testRepository.getRef(refBase + defaultBranch).getObject().getSha();

            Map<String, GHBranch> branches = testRepository.getBranches();
            GHRef newBranchRef;
            if (branches.containsKey(newBranchName)) {
                newBranchRef = testRepository.getRef(refBase + newBranchName);
            } else {
                newBranchRef = testRepository.createRef(refBase + newBranchName, defaultBranchSha);
            }

            GHContent fileContentToModify = testRepository.getFileContent(fileName, newBranchRef.getRef());

            // Assume clean format
            String fileContentAsString = new String(fileContentToModify.read().readAllBytes(), StandardCharsets.UTF_8);
            String newFileContentAsString = fileContentAsString.replace(dependency + ":" + currentVersion, dependency + ":" + newVersion);

            // Create the commit
            GHCommit newBranchLatestCommit = testRepository.getCommit(newBranchRef.getObject().getSha());
            GHTreeBuilder ghTreeBuilder = testRepository.createTree().baseTree(newBranchLatestCommit.getTree().getSha());
            ghTreeBuilder.add(fileName, newFileContentAsString, false);
            GHTree ghTree = ghTreeBuilder.create();
            GHCommit updateDependencyVersionCommit = testRepository.createCommit()
                    .parent(newBranchLatestCommit.getSHA1())
                    .tree(ghTree.getSha())
                    .message(newCommitMessage)
                    .create();

            // Create a PR from new to default branch
            newBranchRef.updateTo(updateDependencyVersionCommit.getSHA1());
            testRepository.createPullRequest(newTitle, newBranchRef.getRef(), refBase + defaultBranch, "new PR")
            ;
        } catch (IOException exception) {
            System.out.println((exception));
        }
    }
}
