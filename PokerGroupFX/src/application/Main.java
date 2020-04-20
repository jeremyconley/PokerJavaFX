package application;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Stack;

import application.GameController.HandState;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

public class Main extends Application {
	PokerTablePane pokerTable;
	ControlsPane controlPane;
	PayoutViewPane payoutViewPane;
	//Array to check which cards are selected for discard. (1 if selected, 0 if not)
	private int[] cardsToDiscard = {0,0,0,0,0};
	
	//Lists of total Cards and Current Deck
	private ArrayList<Card> cardList = new ArrayList<Card>();
	private Stack<Card> deck = new Stack<Card>();
	
	//List of Player's Current Hand
	private ArrayList<Card> currentHand = new ArrayList<Card>();
	
	//Game Controller
	GameController gameController;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			//Main Scene Setup w/ Black BG
			VBox root = new VBox();
			Scene scene = new Scene(root,600,600);
			BackgroundFill background_fill = new BackgroundFill(Color.BLACK, null, null); 
			Background background = new Background(background_fill);
			root.setBackground(background);
			
			//Initialize Game Controller
			gameController = new GameController();
			
			//Setup Scene
			setupSceneViews(root);
			
			//Add the Cards
			fillCardList();
			
			//Show the scene
			primaryStage.setScene(scene);
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	void setupSceneViews(VBox root) {
		//Setup Payout Info Pane (Top View)
		payoutViewPane = new PayoutViewPane();
		root.getChildren().add(payoutViewPane);	
		
		//Setup Poker Table View (Mid View)
		pokerTable = new PokerTablePane();
		root.getChildren().add(pokerTable);
		
		//Setup Controls View (Bottom View)
		controlPane = new ControlsPane();
		root.getChildren().add(controlPane);
	}
	
	//Loop over images names, create card items with value and suit
	public  void fillCardList() {
		cardList.clear();
		for (int i = 1; i <= 13; i++) {
			//Add Spades
			Card card = new Card(i, 0, "images/" + i + ".png");
			cardList.add(card);
		}
		for (int i = 14; i <= 26; i++) {
			//Add Hearts
			Card card = new Card(i - 13, 1, "images/" + i + ".png");
			cardList.add(card);
		}
		for (int i = 27; i <= 39; i++) {
			//Add Diamonds
			Card card = new Card(i - 26, 2, "images/" + i + ".png");
			cardList.add(card);
		}
		for (int i = 40; i <= 52; i++) {
			//Add Diamonds
			Card card = new Card(i - 39, 3, "images/" + i + ".png");
			cardList.add(card);
		}
	}
	
	//Deal Cards Method
	public void dealCards() {
		if (gameController.handState == HandState.START) {
			//Update hand state
			gameController.updateState();
			deck.clear(); //Clear the deck
			currentHand.clear(); //Clear Current hand
			Collections.shuffle(cardList); //Shuffle the cards
			deck.addAll(cardList); //Add shuffled cards to the Deck
			for (int i = 0; i <= 4; i++) {
				//Deal & set the first five off the top of the deck
				Card cardToDeal = deck.pop();
				pokerTable.cardViews.get(i).setImage(new Image(cardToDeal.imageName)); //Set Card Images
				currentHand.add(i, cardToDeal); //Upload your current hand
			}
			pokerTable.dealAnimation();//Call animation for dealing cards
			
			controlPane.enableBetting(); //Allow betting
			
			for (int i = 0; i <=4; i++) { //Reset for new hand
				cardsToDiscard[i] = 0; //Reset Discard Cards to 0
			}
		}
	}
	//Draw Cards Method
	public void drawCards() {
		if (gameController.handState == HandState.DRAW) {
			//Update hand state
			gameController.updateState();
			
			controlPane.disableBetting(); //Deactivate Betting
			
			//Check if no cards are selected (To skip animations & evaluate)
			boolean noneSelected = true;
			for (int i = 0; i <= 4; i++) {
				if ((cardsToDiscard[i] == 1)) { //If any card is selected, this will be false
					noneSelected = false;
				}
			}

			if (noneSelected) {
				evaluateHand();
				return;
			}
			
			for (int i = 0; i <= 4; i++) { 
				if (cardsToDiscard[i] == 1) { //Loop and see which cards are selected
					Card newCard = deck.pop(); //Get next card on the deck
					currentHand.set(i, newCard);//Upload your current hand
					//Call Animation
					pokerTable.drawAnimation(pokerTable.cardViews.get(i), newCard.imageName);
				}
			}
		}
	}
	
	//Evaluate Hand
	public void evaluateHand() {
		if (gameController.handState == HandState.END) {
			
			//Evaluate Stuff...
			
			//Sort and split hand
			currentHand.sort(new CardComparator());	
			int u,v,x,y,z; //integer values of cards
			u = currentHand.get(0).value;
			v = currentHand.get(1).value;
			x = currentHand.get(2).value;
			y = currentHand.get(3).value;
			z = currentHand.get(4).value;
			
			//Check for flush
			boolean flush = true;
			int suit = currentHand.get(0).suit;
			for (int i = 1; i < currentHand.size(); i++) {
				if (currentHand.get(i).suit != suit) {
					flush = false;
				}
			}
			//Check for straight
			boolean straight = true;
			for (int i = 1; i < currentHand.size(); i++) {
				if (i != currentHand.get(i - 1).value + 1) {
					straight = false;
				}
				//Special Case for Ace at the end
				if ((u == 1) && (v == 10) && (v == 11) && (v == 12) && (z == 13)) {
					straight = true;
				}
			}
			
			//Straight Flush && Royal Flush
			boolean royalFlush = false;
			if (straight && flush) {
				if ((u == 1) && (z == 13)) {
					royalFlush = true;
				}
			}
			
			//Four of Kind
			boolean fourKind = false;
			if ((u == v && v == x && x == y) || (v == x && x == y && y == z)) {
				fourKind = true;
			}
			//4 Kind, Full House, 3 Kind, Two Pair, Pair
			boolean fullHouse = false;
			boolean threeKind = false;
			boolean twoPair = false;
			boolean pair = false;
			if ((u == v && v == x && x == y) || (v == x && x == y && y == z)) {//4 Kind
				fourKind = true; 
			} else if (((u == v && v == x) && (y == z)) || ((u == v) && (x == y && y == z))) {//Full House
				fullHouse = true;
			} else if ((u == v && v == x) || (v == x && x == y)) {// 3 Kind
				threeKind = true;
			} else if ((u == v) && (x == y) || (u == v) && (y == z) || (v == x) && (y == z)) {//Two Pair
				twoPair = true;
			} else if ((u == v) || (v == x) || (x == y) || (y == z)) {//Pair
				//Check for Jacks or Better
				if ((u == v) && ((u + v >= 22) || (u + v == 2))) {
					//Check if pair, if both are at least a Jack or Ace's
					pair = true;
				} else if ((v == x) && ((v + x >= 22) || (v + x == 2))) {
					//Check if pair, if both are at least a Jack or Ace's
					pair = true;
				} else if ((x == y) && ((x + y >= 22) || (x + y == 2))) {
					//Check if pair, if both are at least a Jack or Ace's
					pair = true;
				} else if ((y == z) && ((y + z >= 22) || (y + z == 2))) {
					//Check if pair, if both are at least a Jack or Ace's
					pair = true;
				}
			}
						
			Hand handPlayed = new Hand();
			
			//Check for win
			if (royalFlush) {
				handPlayed = payoutViewPane.royalFlush; //Set hand played
			} else if (straight && flush) {
				handPlayed = payoutViewPane.straightFlush; //Set hand played
			} else if (fourKind) {
				handPlayed = payoutViewPane.fourKind; //Set hand played
			} else if (fullHouse) {
				handPlayed = payoutViewPane.fullHouse; //Set hand played
			} else if (flush) {
				handPlayed = payoutViewPane.flush; //Set hand played
			} else if (straight) {
				handPlayed = payoutViewPane.straight; //Set hand played
			} else if (threeKind) {
				handPlayed = payoutViewPane.threeKind; //Set hand played
			} else if (twoPair) {
				handPlayed = payoutViewPane.twoPair; //Set hand played
			} else if (pair) {
				handPlayed = payoutViewPane.jacksOrBetter; //Set hand played
			} else {
				//Loss Case (Not the best LOL..)
				handPlayed.name = "L";
			}
			
			if (handPlayed.name != "L") {
				//Handle Win
				gameController.incrementChips(gameController.betAmount * handPlayed.payout1); //Set winnings amount
				//Set Result View info
				pokerTable.resultView.handResultText.setText("You Win!"); //Win Text
				pokerTable.resultView.hand.setText(handPlayed.name); //Hand Name
				pokerTable.resultView.winningsLabel.setText("+" + gameController.betAmount * handPlayed.payout1);//Pay-out
				
				//Update Chip Count Label
				controlPane.chipCountLabel.setText("Chips: " + gameController.chipCount);
			
			} else {
				//Handle Loss
				gameController.decrementChips(gameController.betAmount); //Set loss amount
				//Set Result View info
				pokerTable.resultView.handResultText.setText("You Lose"); //Loss Text
				pokerTable.resultView.hand.setText(""); //Hand Name Set to empty
				pokerTable.resultView.winningsLabel.setText("-" + gameController.betAmount);//Loss
				
				//Update Chip Count Label
				controlPane.chipCountLabel.setText("Chips: " + gameController.chipCount);
			}
			
			//Done Evaluating Stuff...
			
			pokerTable.resultView.fadeInOut();
			gameController.updateState(); //Game state set back to START
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	//Container For Game Controls (Bottom View)
	class ControlsPane extends VBox {
		Button dealButton;
		Button drawButton;
		TextField betInput;
		RadioButton betMin;
		RadioButton betMax;
		Text chipCountLabel;
		
		public ControlsPane() {
			//Add Deal Button
			dealButton = new Button("Deal");
			dealButton.setStyle("-fx-background-color: #F3FF00; -fx-text-fill: #000000; -fx-font-size: 1.4em;");
			//Deal Button Handler
			dealButton.setOnAction(e -> {
				dealCards();
			});
			//Add Draw Button
			drawButton = new Button("Draw");
			drawButton.setStyle("-fx-background-color: #F3FF00; -fx-text-fill: #000000; -fx-font-size: 1.4em;");
			//Draw Button Handler
			drawButton.setOnAction(e -> {
				drawCards();
			});
			//Add Container for buttons
			FlowPane dealDrawContainer = new FlowPane();
			//Style Container
			dealDrawContainer.setPadding(new Insets(15, 0, 0, 10));
			dealDrawContainer.setHgap(20);
			//Add Buttons
			dealDrawContainer.getChildren().addAll(dealButton, drawButton);
			
			//Add Container with radio buttons and text field for betting
			FlowPane betContainer = new FlowPane();
			
			//Bet Text Field and Label
			Text betInputLabel = new Text("Bet Other: ");
			betInputLabel.setFill(Color.YELLOW);
			betInput = new TextField();
			//Initially Set bet input field to Min bet (1), and Disable
			betInput.setDisable(true);
			betInput.setText("1");
			
			//Bet Input Handler
			
			//Called when selected
			betInput.setOnMouseClicked(e -> { 
				betMin.setSelected(false); //Unselect min / max
				betMax.setSelected(false);
				payoutViewPane.highlightPayoutView(1); //Highlight middle payout pane
	        });
			//Called when changed
			betInput.textProperty().addListener((obs, oldText, newText) -> { 
				
				int betInputAmount;
				try {
					betInputAmount = Integer.parseInt(newText); //Set new bet amount
				  } catch (NumberFormatException e) {
					//If odd character is entered, we will use old bet amount
					betInputAmount = Integer.parseInt(oldText);
				  }
				
				if (betInputAmount > gameController.chipCount) { //User entered value > chip count
					betInputAmount = gameController.chipCount; //Set to max bet
				}
				
				//Update text field text if odd char was entered, or if input > max
				betInput.setText(betInputAmount + "");
				
				//Update Pay out values with respect to new bet amount, highlight current payout
				payoutViewPane.calculatePayout(betInputAmount);
				payoutViewPane.highlightPayoutView(1);
				
				//Update betAmount to input amount
				gameController.placeCustomBet(betInputAmount);
			});
			
			//Min bet
			betMin = new RadioButton("Bet 1");
			betMin.setTextFill(Color.YELLOW);
			//Add Bet handler
			betMin.setOnAction(e -> {
				gameController.betSelection(0, betInput);
				
				//Toggle Buttons
				betMin.setSelected(true);
				betMax.setSelected(false);
				//Highlight minimum pay-out pane
				payoutViewPane.highlightPayoutView(0);
				
			});
			//Max Bet
			betMax = new RadioButton("Bet Max");
			betMax.setTextFill(Color.YELLOW);
			//Add Bet handler
			betMax.setOnAction(e -> {
				gameController.betSelection(1, betInput);
					
				//Toggle Buttons
				betMin.setSelected(false);
				betMax.setSelected(true);
				
				//Highlight maximum pay-out pane
				payoutViewPane.highlightPayoutView(2);
			});
			
			//Initially Disable Bet Options (Wait until Draw State)
			betMin.setDisable(true);
			betMax.setDisable(true);
			
			//Add Buttons and TexField
			betContainer.getChildren().addAll(betMin, betMax, betInputLabel, betInput);
			//Style Container
			betContainer.setPadding(new Insets(15, 0, 15, 10));
			betContainer.setHgap(15);
			
			//Add container for chip count label, and radio buttons for number of decks used
			chipCountLabel = new Text("Chips: 250");
			chipCountLabel.setFill(Color.YELLOW);
			chipCountLabel.setTranslateX(10);
			FlowPane numOfDecksContainer = new FlowPane();
			Text deckCountLabel = new Text("Number of Decks: ");
			deckCountLabel.setFill(Color.YELLOW);
			RadioButton oneDeck = new RadioButton("1");
			oneDeck.setTextFill(Color.YELLOW);
			RadioButton twoDeck = new RadioButton("2");
			twoDeck.setTextFill(Color.YELLOW);
			RadioButton threeDeck = new RadioButton("3");
			threeDeck.setTextFill(Color.YELLOW);
			numOfDecksContainer.setPadding(new Insets(15, 0, 0, 10));
			numOfDecksContainer.setHgap(15);
			numOfDecksContainer.getChildren().addAll(deckCountLabel, oneDeck, twoDeck, threeDeck);
			
			
			this.getChildren().addAll(dealDrawContainer, betContainer, chipCountLabel, numOfDecksContainer);
			this.setMinHeight(320);
		}
		
		public void enableBetting() {
			//Initially set min bet
			betMin.setSelected(true);
			//Highlight first payout
			payoutViewPane.highlightPayoutView(0);
			
			//Enable bet buttons and text field
			betMin.setDisable(false);
			betMax.setDisable(false);
			betInput.setDisable(false);
		}
		public void disableBetting() {
			//Disable bet buttons and text field
			betMin.setDisable(true);
			betMax.setDisable(true);
			betInput.setDisable(true);
			
		}
	}
	
	class PokerTablePane extends StackPane {
		//Lists for ImageViews, Total Cards, and a Stack for the Deck
		public ArrayList<ImageView> cardViews = new ArrayList<ImageView>();
		FlowPane cardPane = new FlowPane(); //Pane for card
		
		//Sizes for Cards and Scene
		int CARD_HEIGHT = 150;
		int CARD_WIDTH = 114;
		int SCENE_WIDTH = 600;
		int SCENE_HEIGHT = 280;
		
		ResultViewPane resultView = new ResultViewPane();
		
		public PokerTablePane() {
			//Black Background
			Rectangle bg = new Rectangle(0,0,SCENE_WIDTH,SCENE_HEIGHT);
			bg.setFill(Color.DARKBLUE);
			this.getChildren().add(bg);
			//Setup Card Views
			cardPane.setHgap(5);
			cardPane.setPadding(new Insets(55,5,0,5));
			for (int i = 0; i < 5; i++) { //Add Five Card Image Views
				cardViews.add(new ImageView(new Image("images/b2fv.png"))); //Set to back of card
				cardViews.get(i).setFitHeight(CARD_HEIGHT);
				cardViews.get(i).setFitWidth(CARD_WIDTH);
				int index = i;
				cardViews.get(i).setOnMouseClicked(e -> {
		            selectForDiscard(index);
		        });
				cardPane.getChildren().add(cardViews.get(i));
			}
			
			this.getChildren().addAll(cardPane, resultView);
		}
		
		public void resetCards() {
			for (int i = 0; i < 5; i++) { //Add Five Card Image Views
				cardViews.get(i).setImage(new Image("images/b2fv.png"));
			}
		}
		
		public void dealAnimation() {
			for (int i = 0; i <= 4; i++) {
				cardViews.get(i).setTranslateX(-SCENE_WIDTH);
			}
			
			for (int i = 0; i <= 4; i++) { //Loop over card image views
				Random rand = new Random(); //Set a variation of speeds for cards 
				TranslateTransition tt = new TranslateTransition(Duration.millis(700), cardViews.get(i));
				tt.setDelay(Duration.millis(rand.nextInt(120))); //set random delay
				tt.setByX(SCENE_WIDTH); //x amount
			    tt.play(); //Play animation
			}
		}
		
		public void drawAnimation(ImageView iv, String newCardimgName) {
			//Slide out discarded card
			Random rand = new Random(); //Set a variation of speeds for cards
			TranslateTransition tt = new TranslateTransition(Duration.millis(700), iv);
			tt.setDelay(Duration.millis(rand.nextInt(250))); //set random delay
			tt.setByX(-SCENE_WIDTH); //x amount
		    tt.play(); //Play animation
		    tt.setOnFinished(e -> {
		    	//Slide in new card
		    	iv.setRotationAxis(Rotate.Y_AXIS);
		    	iv.setRotate(0); //Rotate img view back in right direction
		    	Random rand2 = new Random(); //Set a variation of speeds for cards
		    	iv.setImage(new Image(newCardimgName)); //Set to image of new card
				TranslateTransition tt2 = new TranslateTransition(Duration.millis(700), iv);
				tt2.setDelay(Duration.millis(rand2.nextInt(250))); //set random delay
				tt2.setByX(SCENE_WIDTH); //x amount
			    tt2.play(); //Play animation
			    
			    tt2.setOnFinished(event -> {
			    	//Called after Cards are drawn
			    	evaluateHand();
			    });
	        });
		    
		}
		
		public void selectForDiscard(int index) {
			if (gameController.handState == HandState.DRAW) {
				//Current card
				Card currentCard = currentHand.get(index);
				//Check if card has already been selected
				boolean isSelected = false;
				if (cardsToDiscard[index] == 0) {
					cardsToDiscard[index] = 1;
				} else {
					isSelected = true;
					cardsToDiscard[index] = 0;
				}
				RotateTransition rt = new RotateTransition(Duration.millis(250), cardViews.get(index));
				rt.setAxis(new Point3D(0,1,0));
			    rt.setByAngle(90);
			    rt.play();
			    boolean cardSelected = isSelected;
			    rt.setOnFinished(e -> {
			    	if (cardSelected) {
			    		cardViews.get(index).setImage(new Image(currentCard.imageName));
			    	} else {
			    		cardViews.get(index).setImage(new Image("images/b2fv.png"));
			    	}
			   
			    	RotateTransition rt90 = new RotateTransition(Duration.millis(250), cardViews.get(index));
			    	rt90.setByAngle(90);
			    	rt90.setAxis(new Point3D(0,1,0));
				    rt90.play();
		        }); 
			}
		}
	}
	
	//Types of hands, Different Pay-outs depending on bet Amount
	class Hand {
		String name;
		int payout1; //Payout for Min bet
		int payout2; //Payout for custom bet
		int payout3; //Payout for max bet
		
		public Hand(String name, int payout1, int payout2, int payout3) {
			this.name = name;
			this.payout1 = payout1;
			this.payout2 = payout2;
			this.payout3 = payout3;
		}
		public Hand() {}
	}
	//Pop up pane after each hand with result (win/loss)
	class ResultViewPane extends VBox {
		Text handResultText = new Text("You Win!");
		Text hand = new Text("Royal Flush");
		Text winningsLabel = new Text("+50");
		public ResultViewPane() {
			//Initially Invisible and Out of Way
			this.setOpacity(0);
			this.setTranslateX(-600); //x pos set to -SCENE_WIDTH
			
			//Style View
			BackgroundFill background_fill = new BackgroundFill(Color.DARKBLUE, null, null); 
			Background background = new Background(background_fill);
			this.setMaxSize(250, 100);
			this.setBackground(background);
			this.setStyle(("-fx-border-style: solid inside;" + 
	                  "-fx-border-width: 2;" +
	                  "-fx-border-insets: 5;" + 
	                  "-fx-border-radius: 5;" + 
	                  "-fx-border-color: red;"));
			
			//Style Text Labels
			handResultText.setFill(Color.YELLOW);
			winningsLabel.setFill(Color.YELLOW);
			hand.setFill(Color.YELLOW);
			this.setAlignment(Pos.CENTER);
			this.getChildren().addAll(handResultText, winningsLabel, hand);
		}
		
		public void fadeInOut() {
			//Disable buttons until after animation is complete
			controlPane.dealButton.setDisable(true);
			controlPane.drawButton.setDisable(true);
			//Slide in invisible view
			this.setTranslateX(0);
		    //After view is in place, fade in
		    FadeTransition inFT = new FadeTransition(Duration.millis(500), this);
			inFT.setFromValue(0);
			inFT.setToValue(1.0);
			inFT.play();
			
			inFT.setOnFinished(e -> {
				//After view is faded in, wait 2 seconds, fade out
				FadeTransition outFT = new FadeTransition(Duration.millis(500), this);
				outFT.setFromValue(1.0);
				outFT.setToValue(0.0);
				outFT.setDelay(Duration.millis(2000));
				outFT.play();
				
				outFT.setOnFinished(event -> {
					//After View is faded out, slide it out of the way
					this.setTranslateX(-600);
					//Reset cards for new hand
					pokerTable.resetCards();
					//Enable Buttons
					controlPane.dealButton.setDisable(false);
					controlPane.drawButton.setDisable(false);
					//Reset Bet Values
					gameController.betAmount = 1;
					controlPane.betInput.setText(gameController.betAmount + "");
				});
			});
		}
	}

	//Pay-out Values Pane
	class PayoutViewPane extends HBox {
		public Hand royalFlush;
		public Hand straightFlush;
		public Hand fourKind;
		public Hand fullHouse;
		public Hand flush;
		public Hand straight;
		public Hand threeKind;
		public Hand twoPair;
		public Hand jacksOrBetter;
		
		Hand[] hands;
		
		VBox handTypesView;
		VBox payout1View;
		VBox payout2View;
		VBox payout3View;
		
		public PayoutViewPane() {
			setupHands();
			hands = new Hand[] {royalFlush, straightFlush, fourKind, fullHouse, flush, straight, 
					threeKind, twoPair, jacksOrBetter};
			this.setSpacing(0);
			this.setBorder(new Border(new BorderStroke(Color.YELLOW, 
		            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			
			calculatePayout(1); //Set Win Values to Min bet initially
		}
		
		//Add all types of hands
		void setupHands() {
			royalFlush = new Hand("ROYAL FLUSH", 800, 16000, 24000);
			straightFlush = new Hand("STRAIGHT FLUSH", 50, 1000, 1500);
			fourKind = new Hand("4 OF A KIND", 25, 500, 750);
			fullHouse = new Hand("FULL HOUSE", 9, 180, 270);
			flush = new Hand("FlUSH", 6, 120, 180);
			straight = new Hand("STRAIGHT", 4, 80, 120);
			threeKind = new Hand("3 OF A KIND", 3, 60, 90);
			twoPair = new Hand("2 PAIR", 2, 40, 60);
			jacksOrBetter = new Hand("JACKS OR BETTER", 1, 20, 30);
		}
		
		void calculatePayout(int betAmt) { //Bet amt passed for text input
			//Royal Flush
			royalFlush.payout2 = betAmt * royalFlush.payout1;//Payout 1 never changes (Min bet), use as multiplier
			royalFlush.payout3 = gameController.chipCount * royalFlush.payout1; //Max earnings
			//Straight Flush
			straightFlush.payout2 = betAmt * straightFlush.payout1;//Payout 1 never changes (Min bet), use as multiplier
			straightFlush.payout3 = gameController.chipCount * straightFlush.payout1; //Max earnings
			//Four Kind
			fourKind.payout2 = betAmt * fourKind.payout1;//Payout 1 never changes (Min bet), use as multiplier
			fourKind.payout3 = gameController.chipCount * fourKind.payout1; //Max earnings
			//Full House
			fullHouse.payout2 = betAmt * fullHouse.payout1;//Payout 1 never changes (Min bet), use as multiplier
			fullHouse.payout3 = gameController.chipCount * fullHouse.payout1; //Max earnings
			//Flush
			flush.payout2 = betAmt * flush.payout1;//Payout 1 never changes (Min bet), use as multiplier
			flush.payout3 = gameController.chipCount * flush.payout1; //Max earnings
			//Straight
			straight.payout2 = betAmt * straight.payout1;//Payout 1 never changes (Min bet), use as multiplier
			straight.payout3 = gameController.chipCount * straight.payout1; //Max earnings
			//Three Kind
			threeKind.payout2 = betAmt * threeKind.payout1;//Payout 1 never changes (Min bet), use as multiplier
			threeKind.payout3 = gameController.chipCount * threeKind.payout1; //Max earnings
			//Two Pair
			twoPair.payout2 = betAmt * twoPair.payout1;//Payout 1 never changes (Min bet), use as multiplier
			twoPair.payout3 = gameController.chipCount * twoPair.payout1; //Max earnings
			//Jacks or Better
			jacksOrBetter.payout2 = betAmt * jacksOrBetter.payout1;//Payout 1 never changes (Min bet), use as multiplier
			jacksOrBetter.payout3 = gameController.chipCount * jacksOrBetter.payout1; //Max earnings
			
			updateUI(); //Update text values
		}
		
		public void updateUI() { //Reset UI with new win values
			this.getChildren().clear();
			
			int paneWidth = (600 / 4) - 7;
			handTypesView = new VBox();
			handTypesView.setMinWidth(paneWidth);
			payout1View = new VBox();
			payout1View.setPadding(new Insets(0,0,0,50));
			payout1View.setMinWidth(paneWidth);
			payout2View = new VBox();
			payout2View.setMinWidth(paneWidth);
			payout2View.setPadding(new Insets(0,0,0,50));
			payout3View = new VBox();
			payout3View.setMinWidth(paneWidth + 50);
			payout3View.setPadding(new Insets(0,0,0,50));
			
			
			Line line1 = new Line(10,155,10,10);
			line1.setStroke(Color.YELLOW);
			line1.setStrokeWidth(1);
			Line line2 = new Line(10,155,10,10);
			line2.setStroke(Color.YELLOW);
			line2.setStrokeWidth(1);
			Line line3 = new Line(10,155,10,10);
			line3.setStroke(Color.YELLOW);
			line3.setStrokeWidth(1);
			
			for (int i = 0; i < hands.length; i ++) {
				Text handName = new Text(hands[i].name);
				handName.setFill(Color.YELLOW);
				handTypesView.getChildren().add(handName);
				
				Text payout1 = new Text("" + hands[i].payout1);
				payout1.setFill(Color.YELLOW);
				payout1View.getChildren().add(payout1);
				
				Text payout2 = new Text("" + hands[i].payout2);
				payout2.setFill(Color.YELLOW);
				payout2View.getChildren().add(payout2);
				
				Text payout3 = new Text("" + hands[i].payout3);
				payout3.setFill(Color.YELLOW);
				payout3View.getChildren().add(payout3);
			}
		
			this.getChildren().addAll(handTypesView, line1, payout1View, line2, payout2View, line3, payout3View);
		}
		
		//Highlight current win prediction pane
		BackgroundFill transparent_background_fill = new BackgroundFill(Color.TRANSPARENT, null, null); 
		Background transparent_background = new Background(transparent_background_fill);
		
		BackgroundFill highlighted_background_fill = new BackgroundFill(Color.RED, null, null); 
		Background highlighted_background = new Background(highlighted_background_fill);
		
		public void highlightPayoutView(int index) { //index: 0 = payout1View, 1 = payout2View..
			if (index == 0) {
				payout1View.setBackground(highlighted_background);
				payout2View.setBackground(transparent_background);
				payout3View.setBackground(transparent_background);
			} else if (index == 1) {
				payout1View.setBackground(transparent_background);
				payout2View.setBackground(highlighted_background);
				payout3View.setBackground(transparent_background);
			} else {
				payout1View.setBackground(transparent_background);
				payout2View.setBackground(transparent_background);
				payout3View.setBackground(highlighted_background);
			}
		}
	}
}

class GameController {
	//Game states: START is before the deal, DRAW is after the deal, END is after the draw
	enum HandState { 
		START, DRAW, END
	}
	int chipCount = 250; //Initial Chip Count
	int betAmount = 1;  //Initial Bet
	HandState handState;
	
	public GameController () {
		handState = HandState.START; //Initial State of hand
	}
	
	public void decrementChips(int amount){//Handle Win
		chipCount -= amount;
	}
	
	public void incrementChips(int amount){//Handle Loss
		chipCount += amount;
	}
	
	public void updateState() { //Upate game state
		switch (handState) {
		case START:
			handState = HandState.DRAW;
			break;
		case DRAW:
			handState = HandState.END;
			break;
		default: 
			handState = HandState.START;
			break;
		}
	}
	
	public int betSelection(int btnIndex, TextField inputField) { //Take in index and textField
		//btnIndex: 0 = min (one chip), 1 = max (all chips)
		if (handState == HandState.DRAW) {
			if (btnIndex == 0) { //min bet
				betAmount = 1;
				inputField.setText("" + betAmount); //Update text field
			} else { //max bet
				betAmount = chipCount;
				inputField.setText("" + betAmount); //Update text field
			}
		}
		return betAmount;
	}
	
	public void placeCustomBet(int amount) { //Called from bet input text field
		betAmount = amount;
	}
}

class CardComparator implements Comparator<Card> {

	@Override
	public int compare(Card o1, Card o2) {
		// TODO Auto-generated method stub
		int val1 = o1.value;
		int val2 = o2.value;
		
		if (val1 < val2) {
			return -1;
		} else if (val1 > val2) {
			return 1;
		} else {
			return 0;
		}
	}
	
}

class Card {
	public int value;
	public int suit; //0=Spade, 1=Heart, 2=Diamond, 3=Club
	public String imageName;
	
	public Card(int value, int suit, String imageName) {
		this.value = value;
		this.suit = suit;
		this.imageName = imageName;
	}
}