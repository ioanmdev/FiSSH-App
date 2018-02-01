package tech.iodev.fissh;

/**
 * Created by ioan on 2/1/18.
 */

public class Computer {

    public int Id;
    public String ComputerIP;
    public String Password;
    public byte[] Certificate;

    public Computer(String computerIP, String password)
    {
        ComputerIP = computerIP;
        Password = password;
    }

    public Computer(int id, String computerIP, String password)
    {
        Id = id;
        ComputerIP = computerIP;
        Password = password;
    }

    public Computer(int id, String computerIP, String password, byte[] certificate)
    {
        Id = id;
        ComputerIP = computerIP;
        Password = password;
        Certificate = certificate;
    }
}
