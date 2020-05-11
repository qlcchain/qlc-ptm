package com.quorum.tessera.config.cli.admin;

import com.quorum.tessera.cli.CliAdapter;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.config.cli.admin.subcommands.AddPeerCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/** Cli Adapter to be used for runtime updates */
@CommandLine.Command(
        name = "admin",
        subcommands = {AddPeerCommand.class, CommandLine.HelpCommand.class},
        headerHeading = "Usage:%n%n",
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        header = "Admin operations for a Tessera node",
        description =
                "Perform admin tasks such as maintenance or runtime configuration updates, either connecting to "
                        + "an already running node or directly accessing resources")
public class AdminCliAdapter implements CliAdapter, Callable<CliResult> {

    @Override
    public CliType getType() {
        return CliType.ADMIN;
    }

    /**
     * @param args
     * @return CliResult with config object always null.
     */
    @Override
    public CliResult execute(String... args) {
        return new CliResult(0, true, null);
    }

    @Override
    public CliResult call() {
        return this.execute();
    }
}
