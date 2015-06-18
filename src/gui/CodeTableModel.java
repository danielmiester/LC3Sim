/*
 * CodeTableModel.java
 *
 * Created on Feb 20, 2008, 2:44:16 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import cpu.Processor;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Administrator
 */
public class CodeTableModel extends AbstractTableModel implements Observer
{

    private String[] columnNames;
    private Processor proc;

    public CodeTableModel(String[] columns, Processor p)
    {
        columnNames = columns;
        proc = p;
        
    }

    public int getRowCount()
    {
        return proc.mem.length;
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }
    @Override
    public boolean isCellEditable(int row, int col)
    {
        if (col == 0)
            return false;
        return true;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        
        
        switch (columnIndex)
        {
            case 0:
                return value(rowIndex,16);
            case 1:

                return value(proc.mem[rowIndex],2);
            case 2:
                return value(proc.mem[rowIndex],16);
        default:
            return "flipity jibbit";
        }
    }

    @Override
    public String getColumnName(int col)
    {
        
        return columnNames[col];
        
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
        String v = (String)value;
        try{
        if (col == 1)
            proc.mem[row] = (char) Integer.parseInt(v, 2);
        else if (col == 2)
            if (v.startsWith("x")) 
                proc.mem[row] = (char) Integer.parseInt(v.substring(1), 16);
            else
                proc.mem[row] = (char) Integer.parseInt(v, 16);
        }catch(NumberFormatException e){}
        
    }

    

    public void update(Observable ob, Object o)
    {
        
    }
    private String value(int value, int radix)
    {
        
        if (radix == 16)
        {
            String s = Integer.toHexString(value).toUpperCase();
            while(s.length() < 4)
                s = "0" + s;
            return "x" + s;
        }
        else if (radix == 2)
        {
            String s = Integer.toBinaryString(value).toUpperCase();
            while (s.length() < 16)
                s = "0" + s;
            return s;
        }
        return Integer.toString(value, radix).toUpperCase();
    }
}