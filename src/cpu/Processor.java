package cpu;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Observable;
import java.util.Scanner;

/**
 * The main brains of the LC-3. Privides an implementation of most of the LC-3.
 * 
 * @author Daniel De Jager
 * @version v0.1
 */
public class Processor extends Observable implements Runnable {

	/**
	 * The file loaded to initialize the memory.
	 */
	private static final File ROM_FILE = new File("./ROM.dat");

	/**
	 * Values to indicate which operation to perform.
	 */
	private static final char OP_BR = 0;
	private static final char OP_ADD = 0x1;
	private static final char OP_LD = 0x2;
	private static final char OP_ST = 0x3;
	private static final char OP_JSR = 0x4;
	private static final char OP_AND = 0x5;
	private static final char OP_LDR = 0x6;
	private static final char OP_STR = 0x7;
	private static final char OP_RTI = 0x8;
	private static final char OP_NOT = 0x9;
	private static final char OP_LDI = 0xA;
	private static final char OP_STI = 0xB;
	private static final char OP_JMP = 0xC;
	private static final char OP_RESERVED = 0xD;
	private static final char OP_LEA = 0xE;
	private static final char OP_TRAP = 0xF;
	private static final char OP_CODE_MASK = 0xF;

	/**
	 * The default address of the starting of the user program.
	 */
	private static final char START_ADDRESS = 0x3000;

	/**
	 * Address of the Keyboard Status Register.<br>
	 * Bit[15] indicates if a character is waiting in the KDR.
	 */
	public final char KSR = 0xFE00;

	/**
	 * Address of the Keyboard Data Register. <br>
	 * Bits[7:0] holds the ascii value of the last typed key.
	 */
	public final char KDR = 0xFE02;

	/**
	 * Address of the Display Status Register.<br>
	 * if bit 15 is set it indicates that the display is ready for another char.
	 */
	public final char DSR = 0xFE04;

	/**
	 * Address of the Display Data Register. the display will print the char
	 * held at this address.
	 */
	public final char DDR = 0xFE06;

	/**
	 * Address of Machine Control Register. the processor will run as long as
	 * bit 15 is set.
	 */
	public final char MCR = 0xFFFE;

	/**
	 * The actual system memory.
	 */
	public char[] mem = new char[0x10000];

	/**
	 * the Processor local registers r0-r7
	 */
	public char[] R = new char[8];

	/**
	 * The Program Counter. Points to the current (or next) Instruction.
	 */
	public char PC = START_ADDRESS;

	/**
	 * Processor Status Register.<br>
	 * bit[15] is the privilege level.<br>
	 * bit[15] == 0 --> Supervisor Mode.<br>
	 * bit[15] == 1 --> User Mode.<br>
	 * bit[10:8] --> Priority of current process.<br>
	 * bit[2:0] contains the Condition Codes.<br>
	 * bit[2] --> Negative<br>
	 * bit[1] --> Zero<br>
	 * bit[0] --> Positive<br>
	 */
	public char PSR;

	/**
	 * Supervisor Stack Pointer.
	 */
	public char SSP;

	/**
	 * User Stack Pointer.
	 */
	public char USP;

	/**
	 * Instruction Register.
	 */
	public char IR;

	/**
	 * Create a new Processor.
	 */
	public Processor() {
		IR = 0;
		try {
			initialize();
		} catch (FileNotFoundException e) {
			System.err.print("ROM.DAT not found, Unable to continue.\n");
		}

	}

	/**
	 * Runs the processor.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		long time = System.currentTimeMillis() + 100;
		mem[MCR] = (char) (mem[MCR] | (char) 0x8000);
		while ((mem[MCR] & 0x8000) > 0) {
			process();
			if (mem[DDR] != 0)
			{
				System.out.print(mem[DDR]);
				mem[DDR] = 0;
                mem[DSR] = 0x8000;
			}
			
			if (System.currentTimeMillis() > time) {

				setChanged();
				notifyObservers();
				time = System.currentTimeMillis() + 100;
			}

		}
	}

	/**
	 * Stops the processor.
	 */
	public void stop() {
		mem[MCR] = (char) (mem[MCR] & 0xffff);
	}

	/**
	 * Steps to the next Instruction.
	 */
	public void step() {
		process();
		setChanged();
		notifyObservers();
	}

	/**
	 * Actual Brains of the processor.
	 */
	private void process() {
		IR = mem[PC];
		char op = (char) ((IR >> 12) & OP_CODE_MASK);

		switch (op) {
		case OP_ADD:
			if ((IR & 0x20) == 0) {
				R[DR()] = (char) (R[SR1()] + R[SR2()]);
			} else {
				R[DR()] = (char) (R[SR1()] + imm5());
			}
			setcc();
			PC++;
			break;
		case OP_BR:
			boolean n = (((IR >> 11) & 1) == 1);
			boolean z = (((IR >> 10) & 1) == 1);
			boolean p = (((IR >> 9) & 1) == 1);
			boolean N = (((PSR >> 2) & 1) == 1);
			boolean Z = (((PSR >> 1) & 1) == 1);
			boolean P = ((PSR & 1) == 1);
			if ((n && N) || (z && Z) || (p && P)) {
				PC = (char) ((PC + 1) + PCOffset9());
			} else {
				PC++;
			}
			break;
		case OP_LD:
			PC++;
			R[DR()] = mem[PC + PCOffset9()];
			setcc();
			break;
		case OP_ST:
			PC++;
			mem[PC + PCOffset9()] = R[SR()];
			break;
		case OP_JSR:
			PC++;
			R[7] = PC;
			if ((IR & 0x0800) == 0) {
				PC = R[BaseR()];
			} else {
				PC = (char) (PC + PCOffset11());
			}
			break;
		case OP_AND:
			if ((IR & 0x20) == 0) {
				R[DR()] = (char) (R[SR1()] & R[SR2()]);
			} else {
				R[DR()] = (char) (R[SR1()] & imm5());
			}
			setcc();
			PC++;
			break;
		case OP_LDR:
			R[DR()] = mem[R[BaseR()] + offset6()];
			setcc();
			PC++;
			break;
		case OP_STR:
			mem[R[BaseR()] + offset6()] = R[SR()];
			break;
		case OP_RTI:
			if ((PSR & 0x8000) != 0) {
				privilegeException();
			} else
				interrupt();
			break;
		case OP_NOT:
			PC++;
			R[DR()] = (char) (~R[SR1()]);
			setcc();
			break;
		case OP_LDI:
			PC++;
			R[DR()] = mem[mem[PC + PCOffset9()]];
			setcc();
			break;
		case OP_STI:
			PC++;
			mem[mem[PC + PCOffset9()]] = R[SR()];
			break;
		case OP_JMP:
			PC = R[BaseR()];
			break;
		case OP_RESERVED:
			opcodeException();
			break;
		case OP_LEA:
			PC++;
			R[DR()] = (char) (PC + PCOffset9());
			setcc();
			break;
		case OP_TRAP:
			R[7] = (char) (PC + 1);
			PC = mem[IR & 0xff];
			break;
		}
	}

	/**
	 * Sets the condition codes.
	 */
	private void setcc() {

		PSR = (char) (PSR & 0xfff8);
		if (R[DR()] >= 0x8000) {
			PSR = (char) (PSR | 0x4);

		} else if (R[DR()] == 0) {
			PSR = (char) (PSR | 0x2);

		} else {
			PSR = (char) (PSR | 0x1);

		}
	}

	/**
	 * @return the Register for the Source register
	 */
	private char SR() {
		return DR();
	}

	/**
	 * @return the Register for the destination.
	 */
	private char DR() {
		return (char) (7 & (IR >> 9));
	}

	/**
	 * @return Base Register
	 */
	private char BaseR() {
		return SR1();
	}

	/**
	 * @return Source Register 1
	 */
	private char SR1() {
		return (char) (7 & (IR >> 6));
	}

	/**
	 * @return Source Register 2
	 */
	private char SR2() {
		return (char) (7 & IR >> 9);
	}

	/**
	 * Used as the immediate value for the ADD/AND commands
	 * 
	 * @return 5bit value sign-extended to 16 bits.
	 */
	private char imm5() {
		return extend((char) (IR & 0x1f), 5);
	}

	/**
	 * Used as the offset in the STR and LDR commands.
	 * 
	 * @return 6 bit value sign-extended to 16 bits.
	 */
	private char offset6() {
		return extend((char) (IR & 0x3f), 6);
	}

	/**
	 * used as the offset in the memory load/store operations as well as the
	 * branch command.
	 * 
	 * @return 9bit value sign extended to 16 bits
	 */
	private char PCOffset9() {
		return extend((char) (IR & 0x1ff), 9);
	}

	/**
	 * Used in the JSR command.
	 * 
	 * @return a 11 bit value sign-extended to 16 bits.
	 */
	private char PCOffset11() {
		return extend((char) (IR & 0x7ff), 11);
	}

	/**
	 * Sign-extends the given value to 16 bits
	 * 
	 * @param in
	 *            The Value to be extended.
	 * @param bits
	 *            number of input bits.
	 * @return <code>bits</code>-bit <code>in</code> extended to 16 bits.
	 */
	private char extend(char in, int bits) {

		if ((in & (1 << (bits - 1))) > 0)
			return (char) (~in + 1);
		return in;
	}

	/**
	 * Run if a Privilege-mode exception occurs.
	 */
	private void privilegeException() {
		R[6] = SSP;
		mem[SSP++] = PSR;
		mem[SSP++] = PC;
		PC = mem[0x100];
		PSR = (char) (PSR & 0x7FFF);
	}

	/**
	 * Run if the Reserved opcode is encountered.
	 */
	private void opcodeException() {
		R[6] = SSP;
		mem[SSP++] = PSR;
		mem[SSP++] = PC;
		PC = mem[0x101];
		PSR = (char) (PSR & 0x7FFF);
	}

	/**
	 * Fills the memory with the pertinent data. (Equivalent to Booting a
	 * computer)
	 * 
	 * @throws FileNotFoundException
	 */
	public void initialize() throws FileNotFoundException {

		Scanner fs = new Scanner(ROM_FILE);
		char loc = 0;
		while (fs.hasNext()) {
			mem[loc] = (char) (Integer.decode(fs.next()) & 0xffff);
			loc++;
		}
		mem[PSR] = 0x8002;

	}

	/**
	 * called for an interrupt.
	 * (unsure how to implement yet.)
	 */
	private void interrupt() {
		PSR = (char) (PSR | 0x8400);
		R[6] = SSP;
	}
}
