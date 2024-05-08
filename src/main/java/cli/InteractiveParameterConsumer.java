package cli;

import jline.console.ConsoleReader;
import picocli.CommandLine;
import picocli.CommandLine.IParameterConsumer;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;

import java.io.IOException;
import java.util.Stack;

public class InteractiveParameterConsumer  implements IParameterConsumer {

    private final ConsoleReader reader;

    public InteractiveParameterConsumer(ConsoleReader reader) {
        this.reader = reader;
    }

    public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {
        try {
            argSpec.setValue(reader.readLine(String
                    .format("Enter %s: ", argSpec.paramLabel()), '\0'));
        } catch (IOException e) {
            throw new CommandLine.ParameterException(commandSpec.commandLine()
                    , "Error while reading interactively", e, argSpec, "");
        }
    }
}
