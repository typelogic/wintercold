package dx;

import java.io.IOException;
import java.util.EnumSet;

import javax.smartcardio.TerminalFactory;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;

import apdu4j.APDUBIBO;
import apdu4j.CardChannelBIBO;
import apdu4j.TerminalManager;
import apdu4j.HexUtils;
import apdu4j.CommandAPDU;
import apdu4j.ResponseAPDU;

import pro.javacard.gp.ISO7816;

import pro.javacard.gp.GPSession.APDUMode;
import pro.javacard.gp.*;

import pro.javacard.AID;

public class App
{
    static GPSession gp;

    public static void main(String args[]) throws CardException, IOException
    {
        System.out.println("---begin---");
        TerminalFactory tf = TerminalFactory.getDefault();
        CardTerminal reader = null;

        try {
            for (CardTerminal t : tf.terminals().list()) {
                if (t.isCardPresent()) {
                    reader = t;
                    break;
                }
            }
        } catch (CardException e) {
            System.out.println("Error listing card terminals");
            return;
        }

        Card card;
        APDUBIBO channel = null;

        try {
            card = reader.connect("*");
            card.beginExclusive();
            channel = CardChannelBIBO.getBIBO(card.getBasicChannel());
        } catch (CardException e) {
            System.err.println("Could not connect to " + reader.getName() + ": "
                               + TerminalManager.getExceptionMessage(e));
            return;
        }

        gp = GPSession.discover(channel);

        // 1) select cafebabe4201 applet
        AID authAID = AID.fromString("F76964706173730101000101");

        ResponseAPDU r = channel.transmit(new CommandAPDU(
            0x00, ISO7816.INS_SELECT, 0x04, 0x00, authAID.getBytes()));
        System.out.println(
            String.format("SELECT response = 0x%04X", r.getSW()));
        prettyOut(r.getData());

        byte[] k = HexUtils.stringToBin("404142434445464748494a4b4c4d4e4f");
        byte[] kcv = null;
        PlaintextKeys keys = PlaintextKeys.fromMasterKey(k, kcv);
        EnumSet<APDUMode> mode = GPSession.defaultMode.clone();

        for (String s : args) {
            if (s.equals("clear")) {
                System.out.println("-- clear --");
                mode.clear();
            } else if (s.equals("mac")) {
                System.out.println("-- mac --");
                mode.add(APDUMode.fromString("mac"));
            } else if (s.equals("enc")) {
                System.out.println("-- enc --");
                mode.add(APDUMode.fromString("enc"));
            }
        }

        gp.openSecureChannel(keys, null, null, mode);

        test_AP();
        test_AVP();

        System.out.println("--- end ---");
    }

    public static void test_AVP() throws CardException, IOException
    {
        byte data[]
            = {(byte)0x82, (byte)0x00, (byte)0x91, (byte)0x02, (byte)0x10,
               (byte)0x00, (byte)0x7F, (byte)0x2E, (byte)0x86, (byte)0x81,
               (byte)0x84, (byte)0x26, (byte)0x8B, (byte)0x81, (byte)0x29,
               (byte)0xA7, (byte)0x40, (byte)0x2D, (byte)0xAC, (byte)0x91,
               (byte)0x33, (byte)0x57, (byte)0x93, (byte)0x34, (byte)0x2B,
               (byte)0x84, (byte)0x37, (byte)0x81, (byte)0x42, (byte)0x37,
               (byte)0xC2, (byte)0x42, (byte)0x38, (byte)0xD3, (byte)0x42,
               (byte)0x38, (byte)0xE0, (byte)0x42, (byte)0x3E, (byte)0xEE,
               (byte)0x42, (byte)0x3F, (byte)0x4F, (byte)0x43, (byte)0x43,
               (byte)0x3F, (byte)0x44, (byte)0x52, (byte)0x1A, (byte)0x45,
               (byte)0x66, (byte)0x2D, (byte)0x95, (byte)0x6D, (byte)0x66,
               (byte)0x44, (byte)0x70, (byte)0x74, (byte)0x53, (byte)0x79,
               (byte)0xF2, (byte)0x52, (byte)0x7D, (byte)0xE6, (byte)0x42,
               (byte)0x86, (byte)0xEF, (byte)0x42, (byte)0x90, (byte)0x5B,
               (byte)0x86, (byte)0x97, (byte)0x93, (byte)0x92, (byte)0x97,
               (byte)0xA0, (byte)0x91, (byte)0x9A, (byte)0xF3, (byte)0x92,
               (byte)0x9F, (byte)0x8D, (byte)0x94, (byte)0xA2, (byte)0x87,
               (byte)0x8F, (byte)0xA3, (byte)0x94, (byte)0x8F, (byte)0xA4,
               (byte)0xA2, (byte)0x50, (byte)0xAB, (byte)0x85, (byte)0x4C,
               (byte)0xB0, (byte)0xC6, (byte)0x51, (byte)0xB8, (byte)0xCF,
               (byte)0x41, (byte)0xB8, (byte)0xDA, (byte)0x51, (byte)0xCA,
               (byte)0xA0, (byte)0x50, (byte)0xD0, (byte)0x3C, (byte)0x4C,
               (byte)0xD5, (byte)0x4D, (byte)0x5D, (byte)0xD7, (byte)0x17,
               (byte)0x5B, (byte)0xDB, (byte)0xBB, (byte)0x50, (byte)0xE0,
               (byte)0x25, (byte)0x5C, (byte)0xE5, (byte)0x41, (byte)0x5D,
               (byte)0xE7, (byte)0x2C, (byte)0x4C, (byte)0xE7, (byte)0xFE,
               (byte)0x41, (byte)0xF1, (byte)0xB0, (byte)0x5E, (byte)0xF2,
               (byte)0x91, (byte)0x4E, (byte)0xF9, (byte)0xC8, (byte)0x80,
               (byte)0xFC, (byte)0x25, (byte)0x8B};

        ResponseAPDU resp = gp.transmit(
            new CommandAPDU(ISO7816.CLA_ISO7816, 0x2A, 0x00, 0x00, data));
        System.out.println(
            String.format("card response = 0x%04X", resp.getSW()));
    }

    public static void test_AP() throws CardException, IOException
    {
        ResponseAPDU resp = gp.transmit(new CommandAPDU(
            ISO7816.CLA_ISO7816, 0x1A, 0x00, 0x00, new byte[0]));
        System.out.println(
            String.format("card response = 0x%04X", resp.getSW()));
    }

    public static void prettyOut(byte[] msg)
    {
        for (int j = 1; j < msg.length + 1; j++) {
            if (j % 8 == 1 || j == 0) {
                if (j != 0) {
                    System.out.println();
                }
                System.out.format("0%d\t|\t", j / 8);
            }
            System.out.format("%02X", msg[j - 1]);
            if (j % 4 == 0) {
                System.out.print(" ");
            }
        }
        System.out.println();
    }
}
