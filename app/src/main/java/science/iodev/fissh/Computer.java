package science.iodev.fissh;

import java.io.Serializable;

/**
 * Created by ioan on 2/1/18.
 */

class Computer implements Serializable {

    int Id;
    String Nickname;
    String ComputerIP;
    String Password;
    byte[] Certificate;

    Computer(String computerIP, String password)
    {
        ComputerIP = computerIP;
        Password = password;
    }

    Computer(String nickname, String computerIP, String password)
    {
        Nickname = nickname;
        ComputerIP = computerIP;
        Password = password;
    }

    Computer(int id, String nickname, String computerIP, String password, byte[] certificate)
    {
        Id = id;
        Nickname = nickname;
        ComputerIP = computerIP;
        Password = password;
        Certificate = certificate;
    }
}
