package picocli.issue850missingmixin;

import picocli.CommandLine.Option;

/**
 * Reusable options related to a git provider.
 */
public class ProviderMixin {
    @Option(
        names = {"--owner"},
        required = true,
        description = {"The owner of the repository"}
    )
    private String owner;

    @Option(
        names = {"--username"},
        required = true,
        description = {"The username to access the git provider API"}
    )
    private String username;

    @Option(
        names = {"--password"},
        required = true,
        description = {"The password to access the git provider API"}
    )
    private String password;

    @Option(
        names = {"--provider"},
        required = true,
        description = {"The provider of the git repository (${COMPLETION-CANDIDATES})"}
    )
    private GitProvider provider;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public GitProvider getProvider() {
        return provider;
    }

    public void setProvider(GitProvider provider) {
        this.provider = provider;
    }

    static enum GitProvider {
        /**
         * GitHub.
         */
        GITHUB,

        /**
         * Bitbucket Cloud.
         */
        BITBUCKET
    }
}

