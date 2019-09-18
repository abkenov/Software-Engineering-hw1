package kz.edu.nu.cs.se.hw;

import java.util.*;

/**
 * Starter code for a class that implements the <code>PlayableRummy</code>
 * interface. A constructor signature has been added, and method stubs have been
 * generated automatically in eclipse.
 * 
 * Before coding you should verify that you are able to run the accompanying
 * JUnit test suite <code>TestRummyCode</code>. Most of the unit tests will fail
 * initially.
 * 
 * @see PlayableRummy
 *
 *
 */
public class Rummy implements PlayableRummy
{
    private int numberOfPlayers;
    private String[] players = new String[6];
    private Steps currentState;
    private Stack<String> deck = new Stack<>();
    private int currentPlayer;
    private ArrayList<ArrayList<String>> hands = new ArrayList<>();
    private Stack<String> discardPile = new Stack<>();
    private ArrayList<ArrayList<String>> meld = new ArrayList<>();
    private boolean isMelded;
    private String cardFromDiscard = null;

    public Rummy(String... players)
    {
        this.currentPlayer = 0;
        this.numberOfPlayers = players.length;
        this.isMelded = false;

        if(this.numberOfPlayers < 2) throw new RummyException("Error", RummyException.NOT_ENOUGH_PLAYERS);
        if(this.numberOfPlayers > 6) throw new RummyException("Error", RummyException.EXPECTED_FEWER_PLAYERS);

        System.arraycopy(players, 0, this.players, 0, numberOfPlayers);

        this.currentState = Steps.WAITING;

        for(int i = 0; i < this.numberOfPlayers; i++)
        {
            this.hands.add(i, new ArrayList<>());
        }

        final String[] suits = new String[] { "C", "D", "H", "S", "M" };
        final String[] ranks = new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A" };

        for (String suit : suits)
        {
            for (String rank : ranks)
            {
                this.deck.push(rank + suit);
            }
        }
    }

    @Override
    public String[] getPlayers()
    {
        return this.players;
    }

    @Override
    public int getNumPlayers()
    {
        return this.numberOfPlayers;
    }

    @Override
    public int getCurrentPlayer()
    {
        return this.currentPlayer;
    }

    @Override
    public int getNumCardsInDeck()
    {
        return this.deck.size();
    }

    @Override
    public int getNumCardsInDiscardPile()
    {
        return this.discardPile.size();
    }

    @Override
    public String getTopCardOfDiscardPile()
    {
        return this.discardPile.pop();
    }

    @Override
    public String[] getHandOfPlayer(int player)
    {
        if(player >= 0 && player < this.numberOfPlayers)
        {
            return this.hands.get(player).toArray(new String[0]);
        }
        else throw new RummyException("Error", RummyException.NOT_VALID_INDEX_OF_PLAYER);
    }

    @Override
    public int getNumMelds()
    {
        return this.meld.size();
    }

    @Override
    public String[] getMeld(int i)
    {
        if(i < this.meld.size())
        {
            return this.meld.get(i).toArray(new String[0]);
        }
        else throw new RummyException("Error", RummyException.NOT_VALID_INDEX_OF_MELD);
    }

    @Override
    public void rearrange(String card)
    {
        if(this.currentState == Steps.WAITING)
        {
            this.deck.remove(card);
            this.deck.push(card);
        }
        else throw new RummyException("Error", RummyException.EXPECTED_WAITING_STEP);
    }

    @Override
    public void shuffle(Long l)
    {
        if(this.currentState == Steps.WAITING)
        {
            Collections.shuffle(deck, new Random(l));
        }
        else throw new RummyException("Error", RummyException.EXPECTED_WAITING_STEP);
    }

    @Override
    public Steps getCurrentStep()
    {
        return this.currentState;
    }

    @Override
    public int isFinished()
    {
        if(this.currentState == Steps.FINISHED)
        {
            return this.currentPlayer;
        }
        else return -1;
    }

    @Override
    public void initialDeal()
    {
        if (this.currentState == Steps.WAITING)
        {
            this.currentPlayer = 0;

            if (this.numberOfPlayers == 2)
            {
                for (int i = 0; i < 10 * this.numberOfPlayers; i++)
                {
                    this.hands.get(i % this.numberOfPlayers).add(this.deck.pop());
                }
            }
            else if (this.numberOfPlayers < 5)
            {
                for (int i = 0; i < 7 * this.numberOfPlayers; i++)
                {
                    this.hands.get(i % this.numberOfPlayers).add(this.deck.pop());
                }
            }
            else
            {
                for (int i = 0; i < 6 * this.numberOfPlayers; i++)
                {
                    this.hands.get(i % this.numberOfPlayers).add(this.deck.pop());
                }
            }

            this.discardPile.push(this.deck.pop());

            this.currentState = Steps.DRAW;
        }
        else throw new RummyException("Error", RummyException.EXPECTED_WAITING_STEP);
    }

    @Override
    public void drawFromDiscard()
    {
        if(this.currentState == Steps.DRAW)
        {
            if(this.discardPile.size() != 0)
            {
//                this.isDiscarded = true;
                this.cardFromDiscard = this.discardPile.pop();
                this.hands.get(this.currentPlayer).add(this.cardFromDiscard);
                this.currentState = Steps.MELD;
            }
            else throw new RummyException("Error", RummyException.NOT_VALID_DISCARD);
        }
        else throw new RummyException("Error", RummyException.EXPECTED_DRAW_STEP);
    }

    @Override
    public void drawFromDeck()
    {
        if(this.currentState == Steps.DRAW)
        {
            if(deck.size() > 1)
            {
                this.hands.get(this.currentPlayer).add(this.deck.pop());
            }
            else
            {
                while(this.discardPile.size() != 1)
                {
                    this.deck.push(discardPile.pop());
                }
            }

            this.currentState = Steps.MELD;
        }
        else throw new RummyException("Error!", RummyException.EXPECTED_DRAW_STEP);
    }

    public boolean suitChecker(String... cards)
    {
        String temp = cards[0].substring(cards[0].length() - 1);
        for(int i = 1; i < cards.length; i++)
        {
            if(!temp.equals(cards[i].substring(cards[i].length() - 1)))
            {
                return false;
            }
        }
        return true;
    }

    public String[] meldChecker(String... cards)
    {
        int[] cards_ranks = new int[cards.length];

        for(int i = 0; i < cards.length; i++)
        {
            if(cards[i].substring(0, cards[i].length() - 1).equalsIgnoreCase("J"))
            {
                cards_ranks[i] = 11;
            }
            else if(cards[i].substring(0, cards[i].length() - 1).equalsIgnoreCase("Q"))
            {
                cards_ranks[i] = 12;
            }
            else if(cards[i].substring(0, cards[i].length() - 1).equalsIgnoreCase("K"))
            {
                cards_ranks[i] = 13;
            }
            else if(cards[i].substring(0, cards[i].length() - 1).equalsIgnoreCase("A"))
            {
                cards_ranks[i] = 1;
            }
            else
            {
                cards_ranks[i] = Integer.parseInt(cards[i].substring(0, cards[i].length() - 1));
            }
        }
        Arrays.sort(cards_ranks);

        if(suitChecker(cards))
        {
            int temp = cards_ranks[0];
            for(int i = 1; i < cards_ranks.length; i++)
            {
                if(cards_ranks[i] - temp != 1)
                {
                    break;
                }
                else
                {
                    if(i == cards_ranks.length - 1)
                    {
                        return cards;
                    }
                }
                temp = cards_ranks[i];
            }
        }
        else
        {
            int temp = cards_ranks[0];

            for(int i = 1; i < cards_ranks.length; i++)
            {
                if(temp != cards_ranks[i])
                {
                    break;
                }
                else
                {
                    if(i == cards_ranks.length - 1)
                    {
                        return cards;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void meld(String... cards)
    {
        if(this.currentState == Steps.MELD || this.currentState == Steps.RUMMY)
        {
            for(int i = 0; i < cards.length; i++)
            {
                if(!this.hands.get(this.currentPlayer).contains(cards[i]))
                {
                    this.currentState = Steps.DISCARD;
                    throw new RummyException("Error", RummyException.EXPECTED_CARDS);
                }
            }

            if(this.meldChecker(cards) == null)
            {
                this.currentState = Steps.DISCARD;
                throw new RummyException("Error", RummyException.NOT_VALID_MELD);
            }

            List<String> cardslist = new ArrayList<String>(Arrays.asList(cards));
            this.meld.add((ArrayList<String>) cardslist);

            for(String x : cards)
            {
                this.hands.get(this.currentPlayer).remove(x);
            }

            for(int i = 0; i < cards.length; i++)
            {
                this.hands.get(this.currentPlayer).remove(cards[i]);
            }
            this.isMelded = true;

            if(this.hands.get(this.currentPlayer).size() <= 1 && this.currentState == Steps.RUMMY)
            {
                this.currentState = Steps.FINISHED;
            }
            else if(this.hands.get(this.currentPlayer).size() == 0 && this.currentState == Steps.MELD)
            {
                this.currentState = Steps.FINISHED;
            }
        }
        else throw new RummyException("Error", RummyException.EXPECTED_MELD_STEP_OR_RUMMY_STEP);
    }

    @Override
    public void addToMeld(int meldIndex, String... cards)
    {
        if(this.currentState == Steps.MELD || this.currentState == Steps.RUMMY)
        {
            if(cards.length != 0)
            {
                if(this.meldChecker(cards) != null)
                {
                    for(String x : cards)
                    {
                        this.meld.get(meldIndex).add(x);
                        this.hands.get(this.currentPlayer).remove(x);
                    }

                    this.isMelded = true;

                    if(this.hands.get(this.currentPlayer).size() <= 1 && this.currentState == Steps.RUMMY)
                    {
                        this.currentState = Steps.FINISHED;
                    }
                    else if(this.hands.get(this.currentPlayer).size() == 0 && this.currentState == Steps.MELD)
                    {
                        this.currentState = Steps.FINISHED;
                    }
                }
            }
            else throw new RummyException("Error", RummyException.EXPECTED_CARDS);
        }
        else throw new RummyException("Error", RummyException.EXPECTED_MELD_STEP_OR_RUMMY_STEP);
    }

    @Override
    public void declareRummy()
    {
        if(this.currentState == Steps.MELD && this.isMelded == false)
        {
            this.currentState = Steps.RUMMY;
        }
        else throw new RummyException("Error", RummyException.EXPECTED_MELD_STEP);
    }

    @Override
    public void finishMeld()
    {
        if(this.currentState == Steps.MELD || this.currentState == Steps.RUMMY)
        {
            this.isMelded = false;

            this.currentState = Steps.DISCARD;
        }
        else throw new RummyException("Error", RummyException.RUMMY_NOT_DEMONSTRATED);
    }

    @Override
    public void discard(String card)
    {
        if (this.currentState == Steps.DISCARD)
        {
            if(this.hands.get(this.currentPlayer).size() != 0 && !card.equalsIgnoreCase(this.cardFromDiscard))
            {
                if(this.hands.get(this.currentPlayer).indexOf(card) < 0)
                {
                    throw new RummyException("Error", RummyException.EXPECTED_CARDS);
                }

                this.discardPile.push(card);
                this.hands.get(this.currentPlayer).remove(card);
                this.isMelded = false;
//                this.isDiscarded = false;

                if(this.hands.get(this.currentPlayer).size() == 0)
                {
                    this.currentState = Steps.FINISHED;
                }
                else
                {
                    this.currentState = Steps.DRAW;
                    this.currentPlayer = (this.currentPlayer + 1) % this.numberOfPlayers;
                }
            }
            else throw new RummyException("Error", RummyException.EXPECTED_CARDS);
        }
        else throw new RummyException("Error", RummyException.EXPECTED_DISCARD_STEP);
    }

}
