package monpackageclient;

import java.io.IOException;
import java.net.Socket;

import test_jc2.applet2;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadT1Client;
import com.sun.javacard.apduio.CadTransportException;

public class Main {
	final static byte INS_PIN = (byte)0x7F;
    final static short P_PIN = 0x7F01;
    final static short P_PUK = 0x7F02;
    final static short SW_PIN_FAIL = 0x60FF;
    final static short SW_PIN_REQUIRED = 0x61FF;
    final static short SW_CARD_BLOCKED = 0x7FFE;
    final static short SW_CARD_SUSPENDED = 0x7FFD;
    final static short SW_CARD_DEAD = 0x7FFF;
    
    final static byte INS_CHECK_STATUS = (byte)0x80;
    
    final static byte INS_RENT = (byte)0x10;
    final static short SW_RENT_NEGATIVE_BALANCE = 0x6110; 
    
    final static byte INS_RETURN = (byte)0x11;
    final static short SW_RETURN_NO_BIKE = 0x6011;
    
    final static byte INS_CONSULT = 0x20;
    
    final static byte INS_RECHARGE = (byte)0x7B;
    final static short P_RECHARGE_UNIT = 0x7B01;
    final static short P_RECHARGE_MONTH = 0x7B02;
    final static short SW_RECHARGE_EXCEED_MAXIMUM_BALANCE = 0x607B;
	
    
    final static int MAX_PIN_DIGIT = 8;
    final static String PIN_STRING = "PIN";
    final static String PUK_STRING = "PUK";
    
	CadT1Client cad;
	Socket socket;
	Apdu apdu = new Apdu();
	
	public static void main(String[] args) throws IOException, CadTransportException {
		new Main().run();		 
	}
	
	public void connectToCard() {
		
	}
	
	public void disconnectOfCard() {
		
	}
	
	public void run() throws IOException, CadTransportException {
		/* Menu principal */
		 boolean fin = false;
		 while (!fin) {
			 System.out.println();
			 System.out.println("Application cliente Javacard");
			 System.out.println("----------------------------\n");
			 System.out.println("1 - Consulter solde");
			 System.out.println("2 - Inrementer le compteur");
			 System.out.println("3 - Decrementer le compteur");
			 System.out.println("4 - Reinitialiser le compteur");
			 System.out.println("5 - Quitter");
			 System.out.println();
			 System.out.println("Votre choix ?");
			 
			 int choix = System.in.read();
			 while (!(choix >= '1' && choix <= '5')) {
				choix = System.in.read();
			 }
			 
			 apdu.command[Apdu.CLA] = applet2.CLA_MONAPPLET;
			 apdu.command[Apdu.P1] = 0x00;
			 apdu.command[Apdu.P2] = 0x00;
			 
			 switch (choix) {
				 case '1':
					 consulterSolde();
				 break;
	
				 case '2':
				 break;
	
				 case '3':
				 break;
	
				 case '4':
				 break;
	
				 case '5':
				 fin = true;
				 break;
			 }
		 }
	}
	
	public void enterCode(String pinOrPuk) throws IOException, CadTransportException {

		apdu.command[Apdu.INS] = INS_PIN;
		if(PIN_STRING.equals(pinOrPuk)) {
				apdu.command[Apdu.P1] = (byte)(P_PIN & 0xff);
				apdu.command[Apdu.P2] = (byte)((P_PIN >> 8) & 0xff);				
		}
		else if(PIN_STRING.equals(pinOrPuk)) {
				apdu.command[Apdu.P1] = (byte)(P_PUK & 0xff);
				apdu.command[Apdu.P2] = (byte)((P_PUK >> 8) & 0xff);
		}
		else {
				System.out.println("Error, unexpected code type");
				return;
		}
		
		System.out.println("Enter " + pinOrPuk + " code 1 digit at a time (-1 to validate)");
		int i=0;
		boolean quit = false;
		String pinString = "";
		
		while(i < MAX_PIN_DIGIT && !quit) {
			pinString += System.in.read();
		}
		
		short pin = Short.decode(pinString);
		apdu.dataIn[0] = (byte)(pin & 0xff);
		apdu.dataIn[1] = (byte)((pin >> 8) & 0xff);
		
		cad.exchangeApdu(apdu);
		short res = (short)apdu.getStatus();
		switch(res) {
			case 0x900:
				System.out.println("Great Success !");
			break;
			
			case SW_PIN_FAIL:
				System.out.println("Wrong " + pinOrPuk + ", please try again");
				enterCode(pinOrPuk);
			break;
			
			case SW_CARD_BLOCKED:
				System.out.println("The card has been blocked !");
			break;
			
			case SW_CARD_DEAD:
				System.out.println("The card has died in great suffering !");
			break;
			
			default:
				System.out.println("Error inexpected status word");
		}
	}
	
	public void enterPin() throws IOException, CadTransportException {
		enterCode(PIN_STRING);	
	}
	
	public void enterPuk() throws IOException, CadTransportException {
		enterCode(PUK_STRING);
	}
	
	public void consulterSolde() throws IOException, CadTransportException {
		apdu.command[Apdu.INS] = INS_CONSULT;
		cad.exchangeApdu(apdu);
		int res = apdu.getStatus();
		if (res != 0x9000) {
			System.out.println("Erreur : status word different de 0x9000");
		}
		else {
			System.out.println("Montant du solde : " + (int)apdu.dataOut[6]);
			System.out.println("Date du dernier abonnement: " + (int)apdu.dataOut[0] + "/" + (int)apdu.dataOut[1] + "/" 
								+ (short)apdu.dataOut[2]);
		}
	}
	
	public void rechargerSolde() throws IOException, CadTransportException {
		apdu.command[Apdu.INS] = INS_RECHARGE;
		apdu.command[Apdu.P1] = (byte)(P_RECHARGE_UNIT & 0xff);
		apdu.command[Apdu.P2] = (byte)((P_RECHARGE_UNIT >> 8) & 0xff);
		
		System.out.println("Entrer le montant : ");
		short montant = (short)System.in.read();
		apdu.dataIn[0] = (byte)(montant & 0xff);
		apdu.dataIn[1] = (byte)((montant >> 8) & 0xff);
		
		int res = apdu.getStatus();
		if(res != SW_PIN_REQUIRED) {
			System.out.println("Error: should have asked for PIN");
			// block card
		}
		else {
			enterPin();
		}
	}
	
	public byte[] enterDateTime() throws IOException {
		byte[] dateTime = new byte[6];
		
		System.out.println("Enter day (dd)");
		dateTime[0] = (byte) System.in.read();
		
		System.out.println("Enter month (mm)");
		dateTime[1] = (byte) System.in.read();
		
		System.out.println("Enter year (yyyy)");
		short year = (short) System.in.read();
		dateTime[2] = (byte)(year & 0xff);
		dateTime[3] = (byte)((year >> 8) & 0xff);
		
		System.out.println("Enter hour (number of minutes since 0h00)");
		short hour = (short) System.in.read();
		dateTime[4] = (byte)(hour & 0xff);
		dateTime[5] = (byte)((hour >> 8) & 0xff);
		
		return dateTime;
	}
	
	public void prendreVelo() throws IOException, CadTransportException {
		// vérifier carte non bloquée (ni suspendue)
		if(!checkStatus()) return; 
		
		// vérifier qu'un emprunt n'est pas déjà en cours
		//System.out.println("Attention, emprunt en cours, veuillez rendre le vélo avant de pouvoir réemprunter un autre");
		
		byte[] dateTime = enterDateTime();
		
		long biketype;
		do {
			System.out.println("Enter bike type : 0 classic, 1 electric ");
			biketype = (long)System.in.read();
		} while(biketype != 0 && biketype != 1);
		
		// vérifier solde
		apdu.command[Apdu.INS] = INS_CONSULT;
		cad.exchangeApdu(apdu);
		int res = apdu.getStatus();
		if (res != 0x9000) {
			System.out.println("Erreur (INS_CONSULT) : status word different de 0x9000");
			return;
		}
		byte[6] dateDernierAbonnement;
		for(int i=0; i<6; i++) {
			dateDernierAbonnement[i] = apdu.dataOut[i];
		}
		byte solde = apdu.dataOut[6];
		
		//vérifier si il y a un abonnement avant calcul du coup
		short costClassic = 1;
		short costElec = 3;
		if(dateDernierAbonnement[1] != dateTime[1] || (short)dateDernierAbonnement[2] != (short)dateTime[2]){
			//pas d'abonnement en cours
		}
		else {
			// abonnement
			costClassic = 0;
			
			//check le nombre d'élec empruntés dans le mois, si > 5 costElec =1 sinon 3
		}
		short cost = biketype == 0 ? (short)costClassic : (short)costElec;

		System.out.println("Enter bike id (short) : ");
		long IdVelo = (long)System.in.read();
		
		// envoi info à la carte
		// date trois entiers day, month, year
		// time short nb minutes écoulées depuis début de la journée
		// type classique ou électrique short
		// short cost
		// short IDBike
		apdu.command[Apdu.INS] = INS_RENT;
		apdu.dataIn[0] = dateTime[0];
		apdu.dataIn[1] = dateTime[1];
		apdu.dataIn[2] = dateTime[2];
		apdu.dataIn[3] = dateTime[3];
		apdu.dataIn[4] = dateTime[4];
		apdu.dataIn[5] = dateTime[5];
		
		//typeVelo
		apdu.dataIn[6] = (byte)(biketype & 0xff);
		apdu.dataIn[7] = (byte)((biketype >> 8) & 0xff);
		
		//cout
		apdu.dataIn[8] = (byte)(cost & 0xff);
		apdu.dataIn[9] = (byte)((cost >> 8) & 0xff);
		
		//idBike
		apdu.dataIn[10] = (byte)(IdVelo & 0xff);
		apdu.dataIn[11] = (byte)((IdVelo >> 8) & 0xff);
		
		cad.exchangeApdu(apdu);
		switch((short)apdu.getStatus()) {
			case 0x900:
				System.out.println("Success");
			break;
			
			case SW_RENT_NEGATIVE_BALANCE:
			break;
			
			default:
				
			break;	
		}
	}
	
	public void rendreVelo() {
		//rendre vélo
				// date trois entiers day, month, year
				// time short nb minutes écoulées depuis début de la journée
	}
	
	public void debloquerCarte() throws IOException, CadTransportException {
		enterPuk();
	}
	
	public boolean checkStatus() throws IOException, CadTransportException {
		apdu.command[Apdu.INS] = INS_CHECK_STATUS;
		cad.exchangeApdu(apdu);
		
		short res = (short)apdu.getStatus();
		switch(res) {
			case 0x900:
			return true;
		
			case SW_CARD_BLOCKED:
				System.out.println("");
			return false;
			
			case SW_CARD_SUSPENDED:
				System.out.println("");
			return false;
			
			case SW_CARD_DEAD:
				System.out.println("");
			return false;
			
			default:
				System.out.println("Unexpected status word");
				return false;
		}
	}
	
	// demande ID
	
	//demande date et heure courante
}
