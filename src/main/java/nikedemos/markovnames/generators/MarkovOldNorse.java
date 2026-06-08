package nikedemos.markovnames.generators;


import net.minecraft.network.chat.Component;
import nikedemos.markovnames.MarkovDictionary;

public class MarkovOldNorse extends MarkovGenerator {

	public MarkovDictionary markov2;

	public MarkovOldNorse(int seqlen)
	{
		this.markov  = new MarkovDictionary("old_norse_bothgenders.txt",seqlen);
		this.name = Component.translatable("markov.oldNorse").toString();
	}
	
	public MarkovOldNorse()
	{
		this(4); //4 seems best-suited for Old Norse
	}

	@Override
	public String fetch(int gender) //Old Norse names are genderless
	{
		return markov.generateWord();
	}
}
