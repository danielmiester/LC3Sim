/*
 * RegisterModel.java
 *
 * Created on Feb 23, 2008, 4:27:53 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import cpu.Processor;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Administrator
 */
public class RegisterModel extends AbstractTableModel
{

    private static final String[] columnNames = {"Reg.", "Hex", "Dec"};
    private Processor proc;

    public RegisterModel(Processor p)
    {

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
        if (col == 1)
        {
            return true;
        }
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {


        switch (columnIndex)
        {
            case 0:
                if (rowIndex < 8)
                {
                    return "R" + rowIndex;
                } else if (rowIndex == 8)
                {
                    return "PC";
                } else if (rowIndex == 9)
                {
                    return "CC";
                } else if (rowIndex == 10)
                {
                    return "PSR";
                } else if (rowIndex == 11)
                {
                    return "IR";
                }
                break;
            case 1:
                if (rowIndex < 8)
                {
                    return value(proc.R[rowIndex], 16);
                } else if (rowIndex == 8)
                {
                    return value(proc.PC, 16);
                } else if (rowIndex == 9)
                {
                    if ((proc.PSR & 7) == 4)
                    {
                        return "N";
                    }
                    if ((proc.PSR & 7) == 2)
                    {
                        return "Z";
                    }
                    if ((proc.PSR & 7) == 1)
                    {
                        return "P";
                    }
                } else if (rowIndex == 10)
                {
                    return value(proc.PSR, 16);
                } else if (rowIndex == 11)
                {
                    return value(proc.IR, 16);
                }
                break;
            case 2:
                if (rowIndex < 8)
                {
                    return "(" + Integer.toString(proc.R[rowIndex]) + ")";
                } else if (rowIndex == 8)
                {
                    return "(" + Integer.toString(proc.PC) + ")";
                } else if (rowIndex == 9)
                {
                    return null;
                } else if (rowIndex == 10)
                {
                    return "(" + Integer.toString(proc.PSR) + ")";
                } else if (rowIndex == 11)
                {
                    return "(" + Integer.toString(proc.IR) + ")";
                }
                break;
        }
        return null;
    }

    @Override
    public String getColumnName(int col)
    {

        return columnNames[col];
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
        String v = (String) value;
        try
        {
            if (col == 2)
            {
                proc.mem[row] = (char) Integer.parseInt(v, 10);
            } else if (col == 1)
            {
                if (v.startsWith("x"))
                {
                    proc.mem[row] = (char) Integer.parseInt(v.substring(1), 16);
                } else
                {
                    proc.mem[row] = (char) Integer.parseInt(v, 16);
                }
            }
        } catch (NumberFormatException e)
        {
        }
    }

    

    private String value(int value, int radix)
    {

        if (radix == 16)
        {
            String s = Integer.toHexString(value).toUpperCase();
            while (s.length() < 4)
            {
                s = "0" + s;
            }
            return "x" + s;
        } else if (radix == 2)
        {
            String s = Integer.toBinaryString(value).toUpperCase();
            while (s.length() < 16)
            {
                s = "0" + s;
            }
            return s;
        }
        return Integer.toString(value, radix).toUpperCase();
    }
}