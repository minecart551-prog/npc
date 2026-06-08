package noppes.npcs.api.wrapper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IScoreboard;
import noppes.npcs.api.IScoreboardObjective;
import noppes.npcs.api.IScoreboardTeam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScoreboardWrapper implements IScoreboard{
	private Scoreboard board;
	private MinecraftServer server;
	
	protected ScoreboardWrapper(MinecraftServer server){
		this.server = server;
		board = server.getLevel(Level.OVERWORLD).getScoreboard();
	}
	
	@Override
	public IScoreboardObjective[] getObjectives(){
        List<Objective> collection = new ArrayList<Objective>(board.getObjectives());
        IScoreboardObjective[] objectives = new IScoreboardObjective[collection.size()];
        for(int i = 0; i < collection.size(); i++){
        	objectives[i] = new ScoreboardObjectiveWrapper(board, collection.get(i));
        }
        return objectives;
	}

	@Override
	public String[] getPlayerList() {
        Collection<String> collection = board.getObjectiveNames();
		return collection.toArray(new String[collection.size()]);
	}

	@Override
	public IScoreboardObjective getObjective(String name){
		Objective obj = board.getObjective(name);
		if(obj == null)
			return null;
		return new ScoreboardObjectiveWrapper(board, obj);
	}
	
	@Override
	public boolean hasObjective(String objective){
        return board.getObjective(objective) != null;
	}
	
	@Override
	public void removeObjective(String objective){
		Objective obj = board.getObjective(objective); 
		if(obj != null)
			board.removeObjective(obj);
	}
	
	@Override
	public IScoreboardObjective addObjective(String objective, String criteria){
		ObjectiveCriteria icriteria = ObjectiveCriteria.byName(criteria).orElse(null);
        if(icriteria == null)
			throw new CustomNPCsException("Unknown score criteria: %s", criteria);
        
        if(objective.length() <= 0 || objective.length() > 16)
			throw new CustomNPCsException("Score objective must be between 1-16 characters: %s", objective);
        
        Objective obj = board.addObjective(objective, icriteria, Component.translatable(objective), ObjectiveCriteria.RenderType.INTEGER);
        return new ScoreboardObjectiveWrapper(board, obj);
	}

	@Override
	public void setPlayerScore(String player, String objective, int score){
		Objective objec = getObjectiveWithException(objective);
		if(objec.getCriteria().isReadOnly() || score < Integer.MIN_VALUE || score > Integer.MAX_VALUE)
			return;

        Score sco = board.getOrCreatePlayerScore(player, objec);
        sco.setScore(score);
	}
	
	private Objective getObjectiveWithException(String objective){
		Objective objec = board.getObjective(objective);
		if(objec == null)
			throw new CustomNPCsException("Score objective does not exist: %s", objective);
		return objec;
	}

	@Override
	public int getPlayerScore(String player, String objective){
		Objective objec = getObjectiveWithException(objective);
		if(objec.getCriteria().isReadOnly())
			return 0;

        return board.getOrCreatePlayerScore(player, objec).getScore();
	}

	@Override
	public boolean hasPlayerObjective(String player, String objective){
		Objective objec = getObjectiveWithException(objective);
		return board.getPlayerScores(player).get(objec) != null;
	}
	
	@Override
	public void deletePlayerScore(String player, String objective){
		Objective objec = getObjectiveWithException(objective);

        if(board.getPlayerScores(player).remove(objec) != null)
        	board.removePlayerFromTeam(player);
	}

	@Override
	public IScoreboardTeam[] getTeams(){
		List<PlayerTeam> list = new ArrayList<>(board.getPlayerTeams());
		IScoreboardTeam[] teams = new IScoreboardTeam[list.size()];
		for(int i = 0; i < list.size(); i++){
			teams[i] = new ScoreboardTeamWrapper(list.get(i), board);
		}
		return teams;
	}

	@Override
	public boolean hasTeam(String name){
		return board.getPlayerTeam(name) != null;
	}

	@Override
	public IScoreboardTeam addTeam(String name){
		if(hasTeam(name))
			throw new CustomNPCsException("Team %s already exists", name);
		return new ScoreboardTeamWrapper(board.addPlayerTeam(name), board);
	}

	@Override
	public IScoreboardTeam getTeam(String name){
		PlayerTeam team = board.getPlayerTeam(name);
		if(team == null)
			return null;
		return new ScoreboardTeamWrapper(team, board);
	}

	@Override
	public void removeTeam(String name){
		PlayerTeam team = board.getPlayerTeam(name);
		if(team != null)
			board.removePlayerTeam(team);
	}

	@Override
	public IScoreboardTeam getPlayerTeam(String player) {
		PlayerTeam team = this.board.getPlayersTeam(player);
		if(team == null)
			return null;
		return new ScoreboardTeamWrapper(team, board);
	}

	@Override
	public void removePlayerTeam(String player) {
		board.removePlayerFromTeam(player);
	}
}
