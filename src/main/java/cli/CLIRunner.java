package cli;

import crawler.WebCrawler;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.internal.Configuration;
import picocli.CommandLine;
import picocli.shell.jline2.PicocliJLineCompleter;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class CLIRunner {


    @CommandLine.Command(name = "", description = "Example interactive shell with completion",
            footer = {"", "Press Ctrl-C to exit."},
            subcommands = {MyCommand.class})
    static class CliCommands implements Runnable {
        final ConsoleReader reader;
        final PrintWriter out;

        @CommandLine.Spec
        private CommandLine.Model.CommandSpec spec;

        CliCommands(ConsoleReader reader) {
            this.reader = reader;
            out = new PrintWriter(reader.getOutput());
        }

        public void run() {
            out.println(spec.commandLine().getUsageMessage());
        }
    }

    @CommandLine.Command(name = "cmd", mixinStandardHelpOptions = true, version = "1.0",
            description = "Command with some options to demonstrate TAB-completion")
    static class MyCommand implements Runnable {

        @CommandLine.Option(names = {"-iu", "--initial-url"}, description = "Address of the website for crawling/scrapping")
        private String initialUrl;

        @CommandLine.Option(names = {"-nv", "--number-of-visits"}, description = "Number of URLs to be visited")
        private int numberOfVisits;

        @CommandLine.Option(names = {"-fp", "--folder-path"}, description = "Location of the folder where pages will be saved")
        private String folderPath;


        @CommandLine.ParentCommand
        CliCommands parent;

        public void run() {

            if (initialUrl == null) {
                parent.out.println("Hi there. You asked for initial url ");
            }
            if (numberOfVisits == 0) {
                parent.out.println("Hi there. You asked for numberOfVisits ");
            }
            if (folderPath == null) {
                try {
                    folderPath =  new java.io.File(".").getCanonicalPath();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            long startTime = System.currentTimeMillis();
            System.out.println("Starting "+startTime);

            WebCrawler myCrawler = new WebCrawler(initialUrl);
            myCrawler.visitLinksRecursively(numberOfVisits, folderPath);
            System.out.println("Execution in seconds: "+(System.currentTimeMillis() - startTime)/1000);
        }
    }


    public static void main(String[] args) {
        if (!CommandLine.Help.Ansi.AUTO.enabled() && //
                Configuration.getString(TerminalFactory.JLINE_TERMINAL, TerminalFactory.AUTO).toLowerCase()
                        .equals(TerminalFactory.AUTO)) {
            TerminalFactory.configure(TerminalFactory.Type.NONE);
        }

        try {
            ConsoleReader reader = new ConsoleReader();
            CommandLine.IFactory factory = new CustomFactory(new InteractiveParameterConsumer(reader));

            // set up the completion
            CliCommands commands = new CliCommands(reader);
            CommandLine cmd = new CommandLine(commands, factory);
            reader.addCompleter(new PicocliJLineCompleter(cmd.getCommandSpec()));

            // start the shell and process input until the user quits with Ctrl-D
            String line;
            while ((line = reader.readLine("prompt> ")) != null) {
                ArgumentCompleter.ArgumentList list = new ArgumentCompleter.WhitespaceArgumentDelimiter()
                        .delimit(line, line.length());
                new CommandLine(commands, factory)
                        .execute(list.getArguments());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
