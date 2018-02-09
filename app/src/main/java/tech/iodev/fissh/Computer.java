package tech.iodev.fissh;

import java.io.Serializable;

/**
 * Created by ioan on 2/1/18.
 */

public class Computer implements Serializable {

    public int Id;
    public String Nickname;
    public String ComputerIP;
    public String Password;
    public byte[] Certificate;

    public Computer(String computerIP, String password)
    {
        ComputerIP = computerIP;
        Password = password;
    }

    public Computer(String nickname, String computerIP, String password)
    {
        Nickname = nickname;
        ComputerIP = computerIP;
        Password = password;
    }

    public Computer(int id, String computerIP, String password)
    {
        Id = id;
        ComputerIP = computerIP;
        Password = password;
    }

    public Computer(int id, String nickname, String computerIP, String password, byte[] certificate)
    {
        Id = id;
        Nickname = nickname;
        ComputerIP = computerIP;
        Password = password;
        Certificate = certificate;
    }
}
