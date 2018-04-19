/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcsg;

import eu.mihosoft.vcsg.util.VCSGImpl;

import java.io.File;
import java.io.PrintStream;

/**
 *Executes native csg kernel
 * 
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public interface VCSG {

    /**
     * Destroys the currently running vcsg process.
     */
    void destroy();

    /**
     * Returns the process of the current vcsg execution.
     * @return the process of the current vcsg execution
     */
    Process getProcess();

    /**
     * Returns the working directory
     * @return the working directory
     */
    File getWorkingDirectory();

    /**
     * Prints the vcsg output to the specified print streams.
     * @param out standard output stream
     * @param err error output stream
     * @return this interpreter
     */
    VCSG print(PrintStream out, PrintStream err);

    /**
     * Prints the vcsg output to the standard output.
     * @return this interpreter
     */
    VCSG print();



    /**
     * Waits until the vcsg process terminates.
     * @return this interpreter
     */
    VCSG waitFor();

    /**
     * Executes vcsg with the specified script.
     *
     * @param wd working directory (currently ignored)
     * @param arguments arguments
     * @return this shell
     */
    static VCSG execute(File wd, String... arguments) {
        return VCSGImpl.execute(wd, arguments);
    }


    /**
     * Executes vcsg with the specified arguments.
     *
     * @param arguments arguments
     * @return tcc process
     */
    static VCSG execute(String... arguments) {
        return VCSGImpl.execute(arguments);
    }

    /**
     * Returns the vcsg installation folder.
     *
     * @return the vcsg installation folder
     */
    static File getVCSGInstallationFolder() {
        return VCSGImpl.getVCSGRootPath();
    }
}
