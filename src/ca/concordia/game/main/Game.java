package ca.concordia.game.main;


import java.util.HashMap;
import java.util.Map;
import java.awt.Color;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import ca.concordia.game.model.*;
import ca.concordia.game.util.Loader;
import ca.concordia.game.util.Saver;


/**
 * Game class creates a new game, loads and saves a game state. 
 *  @author Pascal,Gustavo,bhavik,Esteban,Diego
 */
public class Game {
	
	private static Game instance = null;
	
	private Player[] players; 
	private Gameboard gameboard;
	private Map<String,Deck> decks;
	private Bank bank;
	private Die die;

	
	public int currentPlayer;
	public int numberOfPlayers;
	
	
	/**
	 * 
	 * Start New Game
	 */
	public String init() {
		
		String colors[] = new String[] {"RED","BLUE","YELLOW","GREEN"};
		
		this.bank = Bank.getInstance();
		
		//Set bank to full since it's a new game.
		int newBalance=120;
		AtomicInteger aNewBalance= new AtomicInteger(newBalance);
		bank.setBankMoney(aNewBalance);
		
		this.decks = new HashMap<String,Deck>();
		//There is exactly 5 decks per game + a Discard Deck
		for(int i = 0 ; i <= 5 ; i++) {
			switch(i) {
				case 0:
					this.decks.put("discard", new Deck("D"));
					break;
				case 1:
					this.decks.put("personalities", new Deck("P"));
					break;
				case 2:
					this.decks.put("cities", new Deck("C"));
					break;
				case 3:
					this.decks.put("events", new Deck("E"));
					break;
				case 4:
					this.decks.put("green", new Deck("G"));
					break;
				case 5:
					this.decks.put("brown", new Deck("B"));
					break;
			}
		}
		
		//Select number of players and their colors.
		//For now we use four players of fixed colors:
		Scanner keyIn=new Scanner(System.in);
		System.out.println("Please select number of players(Maximun => 4):");
		numberOfPlayers = keyIn.nextInt();
		//Close Scanner object
		//keyIn.close();//Don't close until done using in whole proyect.
		
		this.players = new Player[numberOfPlayers];
		
		
		
		for(int i=0; i<numberOfPlayers; i++) {
			this.players[i] = new Player((PersonalityCard)decks.get("personalities").getCard(),colors[i],8,8);
			//Deal 5 green cards to each player:
			decks.get("green").dealCardsToPlayer(players[i],5);
			//Give $10 to each player:
			bank.transferFunds(players[i], 10);
		}
		
		//Initialize Gameboard:
		this.gameboard = new Gameboard();
		
		//Prompt each player to roll dice to pick first player.
		//For now:
		currentPlayer = 0;
		
		return "Run was Successfull";
	}
	/**
	 * Implements Game as a singleton, as there will always be a single game per run.
	 * @return Game
	 */
	public static Game getInstance() {
		if(instance == null) {
			instance = new Game();
		}
		return instance;
	}
	
	/**
	 * Constructor, initializes a new game.
	 */
	public Game() {
		//init();
	}
	
	/**
	 * 
	 * @param gameState
	 * @return
	 */
	public boolean equals(Game gameState){
		//@todo check the number of players
		//@todo check occupied areas
		//@todo check the bank
		return false;
	}
	
	/**
	 * Returns the player who's turn is up.
	 * @return
	 */
	public int nextPlayer() {
		currentPlayer = (currentPlayer + 1)%numberOfPlayers;
		return currentPlayer;
	}
	
	/**
	 * Save game State.In the correct format.
	 */
	public String saveGame() 
	{
		String temp="";
		ArrayList<String> content= new ArrayList<String>();
		
		//Store GameBoard's  Info.
		for(int i=0;i<12;i++)
		{
			temp=temp+this.gameboard.getAreas().get(i).getCityCard().getName()+",";
			temp=temp+this.gameboard.getAreas().get(i).getTroubleMarker()+",";
			temp=temp+this.gameboard.getAreas().get(i).getBuilding()+",";
			temp=temp+this.gameboard.getAreas().get(i).getDemon()+",";
			temp=temp+this.gameboard.getAreas().get(i).getTroll()+",";
			
			//Get Minions of each class
			for(int j=0;j<this.gameboard.getAreas().get(i).getMinions().size();j++)
			{
				temp=temp+this.gameboard.getAreas().get(i).getMinions().get(j).getColor()+",";
			}
			//remove last coma
			temp=removeLastChar(temp);
			//Store new Area info into the content array
			content.add(temp);
			temp="";
		}
		
		
		//Store Player's Info.
		temp=temp+this.players.length;//Get totalNumber of players.
		content.add(temp);
		temp="";
		
		for(int i=0;i<this.players.length;i++)
		{
			temp=temp+ this.players[i].getPersonality().getName()+",";
			temp=temp+ this.players[i].getColor()+",";
			temp=temp+this.players[i].getMinionsOnHand()+",";
			temp=temp+this.players[i].getBuildingOnHand()+",";
			temp=temp+this.players[i].getMoney()+",";
			
			temp=temp+this.players[i].getPlayerCards().size()+",";
			for(int j=0; j<this.players[i].getPlayerCards().size();j++)
			{
				BrownCard bCard = new BrownCard(0);
				GreenCard gCard= new GreenCard(0);
				if(this.players[i].getPlayerCards().get(j).getClass().equals(bCard.getClass())){//The card is of type BrownCard, convert to brown card.
					bCard = (BrownCard) this.players[i].getPlayerCards().get(j);
					temp=temp+bCard.getNumber()+",";
				}else//The card is of type GreenCard, convert to green card.
				{
					gCard=(GreenCard) this.players[i].getPlayerCards().get(j);;
					temp=temp+gCard.getNumber()+",";
				}
				
			}
			temp=temp+this.players[i].getPlayerCityCard().size()+",";
			for(int j=0; j<this.players[i].getPlayerCityCard().size();j++)
			{
				temp=temp+this.players[i].getPlayerCityCard().get(j).getCardNumber()+",";
			}
			//remove last character(the coma)
			temp=removeLastChar(temp);
			//Add new player line to ArrayList.
			content.add(temp);
			temp="";
		}
		temp="";
		//Add the banks current bank balance to arraylist content.
		temp=temp+this.bank.getTotal();
		content.add(temp);
		
		//Write current game's state to 
		Saver.saveGameState(content);
		
		return "Save Was Successfull";
	}
	

	/**
	 * Remove last character of a string if it's a coma.
	 * @param str
	 * @return
	 */
	public String removeLastChar(String str) {
	    if (str.length() > 0 && str.charAt(str.length()-1)==',') {
	      str = str.substring(0, str.length()-1);
	    }
	    return str;
	}
	
	/**
	 * Load a Game State from a txt. file.
	 */
	public String loadGame()
	{
		ArrayList<String> content = new ArrayList<String>();
		
		//First set the game instance to null since there could already be another game running.
		this.instance=null;
		
		//Load SavedGame
		//Create Scanner Object
		Scanner input=new Scanner(System.in);
		String savedGame="";
		
		System.out.println("Please enter the name of the file you wish to load:");
		savedGame = input.next();
		//input.close();
		//Load new gameState into arraylist.
		content=Loader.loadGameState(savedGame);
		
		//Parse Data and create new gameState.
		while(true)
		{
			//Parse Areas from gameboard (Total of 12 Areas).
			this.gameboard.resetAreas();//erase last state of the areas.
			
			//temporary variables.
			String areaName = null;
			boolean troubleMarker = false;
			boolean building = false;
			int demon = 0;
			int troll = 0;
			
			
			for(int i=0;i<12;i++)
			{
				ArrayList<String> minions = new ArrayList<String>();//Array that will contain the color of the minions on a certain area.
				String[] parts = content.get(i).split(",");
				for(int j=0;j<parts.length;j++)
				{
					if(j==0)
						areaName=parts[j];
					else if(j==1)
						troubleMarker=Boolean.valueOf(parts[j]);
					else if(j==2)
						building=Boolean.valueOf(parts[j]);
					else if(j==3)
						demon=Integer.parseInt(parts[j]);
					else if(j==4)
						troll=Integer.parseInt(parts[j]);
					else 
						minions.add(parts[j]);
							
				}
				//Create new city card with the name extracted.
				CityCard cityCard=new CityCard(areaName);
				//Create Area and add to gameboard.
				Area area=new Area(cityCard,troubleMarker,building,demon,troll);
				this.gameboard.addArea(area);//(cityCard,troubleMarker,building,demon,troll) ==> Constructor parameters.
				//Add the minions that where in the current area.
				for(int j=0 ; j<minions.size();j++)
					area.addMinion(new Piece(minions.get(j)));
			}
				
			//savedGame="Test.txt";
			//Get Number of Players(Always at line 12(in array))
			int numberPlayers=Integer.parseInt(content.get(12));
			this.numberOfPlayers=numberPlayers;
			
			//Parse PLayers information.
			this.players=null; //Reset Last state of players.
			this.players = new Player[numberPlayers];//Set new number players.
			
			PersonalityCard perCard = null;
			String color = null;
			int minionOnHand = 0;
			int buildingOnHand = 0;
			int money = 0;
			
			int NumplayerCards = 0;
			int NumcityCards = 0;
			//Start at position 13 after areas and # of players.
			for(int i=13 ; i< 13 +numberPlayers;i++)
			{	
				String[] parts = content.get(i).split(",");
				int playerIndex= i%13;//Index for array players, it starts at 0.
				for(int j=0;j<parts.length;j++)
				{
					if(j==0)
						perCard=new PersonalityCard(parts[j]);
					else if(j==1)
						color=parts[j];
					else if(j==2)
						minionOnHand=Integer.parseInt(parts[j]);
					else if(j==3)
						buildingOnHand=Integer.parseInt(parts[j]);
					else if(j==4)
						money=Integer.parseInt(parts[j]);
					else if(j==5)
					{
						 NumplayerCards=Integer.parseInt(parts[j]);
						
					}else if(j==(5+NumplayerCards)+1)
					{
						 NumcityCards=Integer.parseInt(parts[j]);
					}
					 
				}
				//Create and add new Player
				this.players[playerIndex]= new Player(perCard,color,minionOnHand,buildingOnHand,money);
				//Add player'sCards
				for(int j=0 ; j<NumplayerCards;j++)
				{
					//Brown cards have int values of 1-48 and green cards have int values of 49-101.
					int checkColor= Integer.parseInt(parts[(5+j)+1]);
					
					Card card= new Card(false,false); //City cards are always visible.
					if(checkColor <49)//This is a Brown card
						 card=new BrownCard(checkColor); 
					else if (checkColor >48)
						card = new GreenCard(checkColor);
					
					this.players[playerIndex].receiveCard(card);
				}
				
				//Add CityCards.
				for(int j=0 ; j<NumcityCards;j++)
				{
					//Get CityCard number.
					int cardNumber= Integer.parseInt(parts[(5+NumplayerCards+(j+1))+1]);
					
					CityCard cC=new CityCard(cardNumber);
					
					this.players[playerIndex].receiveCityCard(cC);
				}
				
				
			}//PLayers
			
			//Parse BankMoney
			for(int i=14+numberPlayers ; i< (13 +numberPlayers)+1;i++)//Get money bank has.
			{
				int bankMoney=Integer.parseInt(content.get(i));
				AtomicInteger aInt= new AtomicInteger(bankMoney);
				this.bank.setBankMoney(aInt);//Set new bank balance.
			}
			
			break; //Exit while loop.
		}//While
		
		return "Load Was Successfull";
	}
	
	/**
	 * Prints Information about current game.
	 */
	public void printCurrentState()
	{	
		System.out.println("Current Game Status:"+"\n");
		System.out.println("Number Of Players:"+this.numberOfPlayers);
		System.out.println("Bank balance:"+this.bank.getTotal()+"\n");
		
		System.out.println("Players:");
		for(int i=0;i<this.players.length;i++)
		{
			System.out.println("Player"+(i+1)+":");
			System.out.println(this.players[i].toString());
		}
		
		System.out.println("Game Board State:");
		System.out.println(this.gameboard.toString());
		
	}

}
