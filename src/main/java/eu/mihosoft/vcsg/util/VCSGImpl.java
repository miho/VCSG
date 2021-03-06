package eu.mihosoft.vcsg.util;

import eu.mihosoft.vcsg.VCSG;
import eu.mihosoft.vcsg.vcsgdist.VCSGDist;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Deprecated
public class VCSGImpl implements VCSG {

    private static File executableFile;
    private static File vcsgRootPath;
    private final Process vcsgProcess;
    private static boolean initialized;
    private StreamGobbler errorGobbler;
    private StreamGobbler stdGobbler;


    static {
        // static init
    }
    private final File wd;

    private VCSGImpl(Process proc, File wd) {
        this.vcsgProcess = proc;
        this.wd = wd;
    }

    /**
     * Initializes property folder and executable.
     */
    private static void initialize() {

        // already initialized: we don't do anything
        if (initialized) {
            return;
        }

        try {

            Path confDir
                    = Paths.get(System.getProperty("user.home"), ".vcsg").
                    toAbsolutePath();
            Path distDir = Paths.get(confDir.toString(), "vcsg-dist");
            File base = confDir.toFile();

            if (!Files.exists(confDir)) {
                Files.createDirectory(confDir);
            }

            if (!Files.exists(distDir)) {
                Files.createDirectory(distDir);
            }

            ConfigurationFile confFile
                    = IOUtil.newConfigurationFile(new File(base, "config.xml"));
            confFile.load();
            String timestamp = confFile.getProperty("timestamp");
            File vcsgFolder = distDir.toFile();//, "vcsg");

            String timestampFromDist;

            try {
                Class<?> buildInfoCls = Class.forName("eu.mihosoft.vcsg.vcsgdist.BuildInfo");
                Field timestampFromDistField = buildInfoCls.getDeclaredField("TIMESTAMP");
                timestampFromDistField.setAccessible(true);
                timestampFromDist = (String) timestampFromDistField.get(buildInfoCls);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(VCSGImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(
                        "VCSG distribution for \"" + VSysUtil.getPlatformInfo()
                                + "\" not available on the classpath!", ex);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(VCSGImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(
                        "VCSG distribution for \"" + VSysUtil.getPlatformInfo()
                                + "\" does not contain valid build info!", ex);
            }

            // if no previous timestamp exists or if no vcsg folder exists
            if (timestamp == null || !vcsgFolder.exists()) {
                System.out.println("ts: " + timestamp + ", " + vcsgFolder);
                System.out.println(
                        " -> installing vcsg to \"" + distDir + "\"");
                VCSGDist.extractTo(distDir.toFile());
                confFile.setProperty("timestamp", timestampFromDist);
                confFile.save();
            } else // we need to update the vcsg distribution
                if (!Objects.equals(timestamp, timestampFromDist)) {
                    System.out.println(
                            " -> updating vcsg in \"" + distDir + "\"");
                    System.out.println(" --> current version: " + timestamp);
                    System.out.println(" --> new     version: " + timestampFromDist);
                    VCSGDist.extractTo(distDir.toFile());
                    confFile.setProperty("timestamp", timestampFromDist);
                    confFile.save();
                } else {
                /*System.out.println(
                        " -> vcsg up to date in \"" + distDir + "\""
                );*/
                }

            executableFile = getExecutablePath(distDir);

        } catch (IOException ex) {
            Logger.getLogger(VCSGImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        initialized = true;
    }

    @Override
    public VCSGImpl print(PrintStream out, PrintStream err) {
        if(err!=null) {
            errorGobbler = new StreamGobbler(err, vcsgProcess.getErrorStream(), "");
            errorGobbler.start();
        }
        if(out!=null) {
            stdGobbler = new StreamGobbler(out, vcsgProcess.getInputStream(), "");
            stdGobbler.start();
        }

        return waitFor();
    }

    @Override
    public VCSGImpl print() {
        errorGobbler = new StreamGobbler(System.err, vcsgProcess.getErrorStream(), "");
        errorGobbler.start();

        stdGobbler = new StreamGobbler(System.out, vcsgProcess.getInputStream(), "");
        stdGobbler.start();

        return waitFor();
    }

    @Override
    public VCSGImpl waitFor() {
        try {
            vcsgProcess.waitFor();
            if(errorGobbler!=null) {
                errorGobbler.join();
            }
            if(stdGobbler!=null) {
                stdGobbler.join();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(VCSGImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Cannot wait until process is finished", ex);
        }

        return this;
    }


    public static VCSG execute(String... arguments) {

        initialize();

        Process proc = execute(false, null, arguments);

        return new VCSGImpl(proc, null);
    }


    public static VCSG execute(File wd, String... arguments) {

        initialize();

        Process proc = execute(false, wd, arguments);

        return new VCSGImpl(proc, wd);
    }


    @Override
    public File getWorkingDirectory() {
        return wd;
    }

    /**
     * Calls vcsg with the specified arguments.
     *
     * @param arguments arguments
     * @param wd working directory (currently ignored)
     * @param waitFor indicates whether to wait for process execution
     * @return vcsg process
     */
    public static Process execute(boolean waitFor, File wd, String... arguments) {

        initialize();

        if (arguments == null || arguments.length == 0) {
            arguments = new String[]{"--help"};
        }

        String[] cmd = new String[arguments.length + 1];

        cmd[0] = executableFile.getAbsolutePath();

        for (int i = 1; i < cmd.length; i++) {
            cmd[i] = arguments[i - 1];
        }

        Process proc = null;

        try {
            if(wd==null) {
                proc = Runtime.getRuntime().exec(cmd);
            } else {
                proc = Runtime.getRuntime().exec(cmd, null, wd);
            }
            if (waitFor) {
                proc.waitFor();
            }
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException("Error while executing vcsg", ex);
        }

        return proc;
    }

    @Override
    public Process getProcess() {
        return vcsgProcess;
    }

    /**
     * Destroys the currently running vcsg process.
     */
    @Override
    public void destroy() {
        if (vcsgProcess != null) {
            vcsgProcess.destroy();
        }
    }

    /**
     * Returns the path to the vcsg executable. If the executable has not
     * been initialized this will be done as well.
     *
     * @return the path to the vcsg executable
     */
    private static File getExecutablePath(Path dir) {

        if (!VSysUtil.isOsSupported()) {
            throw new UnsupportedOperationException(
                    "The current OS is not supported: "
                            + System.getProperty("os.name"));
        }

        if (executableFile == null || !executableFile.isFile()) {

            vcsgRootPath = dir.toFile();// new File(dir.toFile(), "vcsg");

            String executableName;

            if (VSysUtil.isWindows()) {
                executableName = "bin\\occ-csg.exe";
            } else {
                executableName = "bin/occ-csg";
            }

            executableFile = new File(vcsgRootPath, executableName);

            if (!VSysUtil.isWindows()) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{
                            "chmod", "u+x",
                            executableFile.getAbsolutePath()
                    });

                    InputStream stderr = p.getErrorStream();

                    BufferedReader reader
                            = new BufferedReader(
                            new InputStreamReader(stderr));

                    String line;

                    while ((line = reader.readLine()) != null) {
                        System.out.println("Error: " + line);
                    }

                    p.waitFor();
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(VCSGImpl.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
        }

        return executableFile;
    }

    /**
     * Unzips specified source archive to the specified destination folder. If
     * the destination directory does not exist it will be created.
     *
     * @param archive archive to unzip
     * @param destDir destination directory
     * @throws IOException
     */
    public static void unzip(File archive, File destDir) throws IOException {
        IOUtil.unzip(archive, destDir);
    }

    /**
     * Saves the specified stream to file.
     *
     * @param in stream to save
     * @param f destination file
     * @throws IOException
     */
    public static void saveStreamToFile(InputStream in, File f) throws IOException {
        IOUtil.saveStreamToFile(in, f);
    }

    public static File getVCSGRootPath() {
        initialize();

        return vcsgRootPath;
    }


}
// based on http://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki

class StreamGobbler extends Thread {

    private  volatile InputStream is;
    private  volatile String prefix;
    private  volatile PrintStream pw;

    StreamGobbler(PrintStream pw, InputStream is, String prefix) {
        this.is = is;
        this.prefix = prefix;
        this.pw = pw;
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                pw.println(prefix + line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

}