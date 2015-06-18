/*
 * StartStopAction.java
 *
 * Created on Feb 22, 2008, 3:41:26 AM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.Actions;

import cpu.Processor;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author Administrator
 */
public class StartStopAction extends AbstractAction
{

    private Processor p;
    private Thread t;

    public StartStopAction(Processor proc)
    {
        super("Run");
        p = proc;
    }

    public void actionPerformed(ActionEvent e)
    {
        if (super.getValue(NAME).equals("Run"))
        {
            super.putValue(NAME, "Stop");
            t = new Thread(p);
            t.start();
            
        } else
        {
            super.putValue(NAME, "Run");
            p.mem[p.MCR] = 0;
        }
    }
}