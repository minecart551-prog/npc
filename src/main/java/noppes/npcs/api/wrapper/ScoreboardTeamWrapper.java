package noppes.npcs.api.wrapper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IScoreboardTeam;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardTeamWrapper implements IScoreboardTeam{
	private PlayerTeam team;
	private Scoreboard board;
	protected ScoreboardTeamWrapper(PlayerTeam team, Scoreboard board){
		this.team = team;
		this.board = board;
	}
	
	@Override
	public String getName(){
		return team.getName();
	}

	@Override
	public String getDisplayName(){
		return team.getDisplayName().getString();
	}
	
	@Override
	public void setDisplayName(String name){
		if(name.length() <= 0 || name.length() > 32)
			throw new CustomNPCsException("Score team display name must be between 1-32 characters: %s", name);
		team.setDisplayName(Component.translatable(name));
	}

	@Override
	public void addPlayer(String player){
		board.addPlayerToTeam(player, team);
	}

	@Override
	public void removePlayer(String player){
		board.removePlayerFromTeam(player, team);
	}

	@Override
	public String[] getPlayers(){
		List<String> list = new ArrayList<String>(team.getPlayers());
		return list.toArray(new String[list.size()]);
	}

	@Override
	public void clearPlayers(){
        List<String> list = new ArrayList<String>(team.getPlayers());
        for(String player : list){
        	board.removePlayerFromTeam(player, team);
        }
	}

	@Override
	public boolean getFriendlyFire(){
		return team.isAllowFriendlyFire();
	}

	@Override
	public void setFriendlyFire(boolean bo){
		team.setAllowFriendlyFire(bo);
	}
	
	@Override
	public void setColor(String color){
		ChatFormatting enumchatformatting = ChatFormatting.getByName(color);

        if (enumchatformatting == null || enumchatformatting.isFormat())
        	throw new CustomNPCsException("Not a proper color name: %s", color);

        team.setPlayerPrefix(Component.literal(enumchatformatting.toString()));
        team.setPlayerSuffix(Component.literal(ChatFormatting.RESET.toString()));
	}
	
	@Override
	public String getColor(){
		Component prefix = team.getPlayerPrefix();
		if(prefix == null || prefix.getString().isEmpty())
			return null;
		for(ChatFormatting format : ChatFormatting.values()){
			if(prefix.getString().equals(format.toString()) && format != ChatFormatting.RESET)
				return format.getName();
		}
		return null;
	}

	@Override
	public void setSeeInvisibleTeamPlayers(boolean bo){
		team.setSeeFriendlyInvisibles(bo);
	}

	@Override
	public boolean getSeeInvisibleTeamPlayers(){
		return team.canSeeFriendlyInvisibles();
	}

	@Override
	public boolean hasPlayer(String player) {		
		return board.getPlayersTeam(player) != null;
	}
}
