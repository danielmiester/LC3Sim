package main;

import gui.LC3Gui;

public class LC3Main
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            public void run()
            {
                new LC3Gui().setVisible(true);
            }
        });
    }
}