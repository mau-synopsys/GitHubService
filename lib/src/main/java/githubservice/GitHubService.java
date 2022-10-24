package githubservice;

import org.kohsuke.github.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

public class GitHubService {
    public static GitHub buildServiceViaLibrary(String accessToken) throws IOException {
        return new GitHubBuilder().withOAuthToken(accessToken).build();
    }

    public static void main(String[] args) {
        try {
            String dependency = "com.google.code.gson";
            String version = "gson:2.9.1";
            String newBranchName = "new-branch";
            // Get repo
            GitHub gitHub = buildServiceViaLibrary("");
            GHRepository ghRepository = gitHub.getRepository("mau-synopsys/test");

            // Branch off default, modify file, and commit
            String defaultBranch = ghRepository.getDefaultBranch();
            String defaultBranchSha = ghRepository.getRef("refs/heads/" + defaultBranch).getObject().getSha();

            Map<String, GHBranch> branches = ghRepository.getBranches();
            GHRef ghRef;
            if (branches.containsKey(newBranchName)) {
                ghRef = ghRepository.getRef("refs/heads/" + newBranchName);
            } else {
                ghRef = ghRepository.createRef("refs/heads/" + newBranchName, defaultBranchSha);
            }

            GHContent ghContent = ghRepository.getFileContent("build.gradle", ghRef.getRef());

            // Assume clean format
            ListIterator<String> itr = (new String(ghContent.read().readAllBytes(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.toList())
                    .listIterator();

            while (itr.hasNext()) {
                String s = itr.next();
                if (s.contains(dependency)) {
                    itr.set("\timplementation " + dependency + " " + version);
                }
            }
            ;
        } catch (IOException exception) {
            System.out.println((exception));
        }
    }
}
