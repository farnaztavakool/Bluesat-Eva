package com.bluesat.eva;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ErrorHandler implements Thread.UncaughtExceptionHandler {

    private File root;
    private Thread.UncaughtExceptionHandler defaultUEH;

    public ErrorHandler( Context context ) {
        this.root = context.getExternalFilesDir( null );
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException( Thread t, Throwable e ) {
        FileWriter fw = null;
        try {
            //File eva = new File( this.root, "EVA" );

            if( !this.root.exists() ) {
                this.root.mkdir();
            }

            File db = new File( this.root, "error.txt" );
            fw = new FileWriter( db.getAbsolutePath(), true );

            this.recursiveLog( fw, e );
        } catch( IOException ex ) {
        } finally {
            try {
                if( fw != null ) {
                    fw.close();
                }
            } catch( IOException ex ) {
            }
        }
        this.defaultUEH.uncaughtException( t, e );
    }

    private void recursiveLog( FileWriter fw, Throwable e ) {
        try {
            fw.append( "Message : " );
            fw.append( e.getMessage() );
            fw.append( "\n" );

            fw.append( "Stack : " );
            for( StackTraceElement trace: e.getStackTrace() ) {
                fw.append( trace.getClassName() );
                fw.append( "." );
                fw.append( trace.getMethodName() );
                fw.append( ":" );
                fw.append( String.valueOf( trace.getLineNumber() ) );
                fw.append( "\n" );
            }

            Throwable cause = e.getCause();
            if( cause != null ) {
                fw.append( "Inner: " );
                this.recursiveLog( fw, cause );
            }
        } catch( IOException ex ) {
        }
    }
}
