package io.github.radixhomework.healthchecker.util;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.ImmutableMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.process.ProcessOutput;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.runtime.Network;
import lombok.NoArgsConstructor;

import java.net.UnknownHostException;

@NoArgsConstructor
public class EmbedMongoDBUtils {

    public static MongodExecutable prepareExecutable(String uri, int port, String username, String password) throws UnknownHostException {
        ImmutableMongodConfig mongodConfig = MongodConfig
                .builder()
                .version(Version.Main.V6_0)
                .net(new Net(uri, port, Network.localhostIsIPv6()))
                .userName(username)
                .password(password)
                .build();

        ProcessOutput processOutput = ProcessOutput.builder()
                .output(Processors.silent())
                .error(Processors.console())
                .commands(Processors.silent())
                .build();

        RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD)
                .processOutput(processOutput)
                .build();

        return MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig);
    }
}
