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
import javax.swing.Action;

/**
 *
 * @author Administrator
 */
public class StepAction extends AbstractAction
{

    private Processor p;

    public StepAction(Processor proc)
    {
        super("Step");
        p = proc;
    }

    public void actionPerformed(ActionEvent e)
    {
       p.step();
    }
}