package xyz.kzkr.vvtalk;

import java.util.HashMap;
import java.util.Objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class VVTalk extends ListenerAdapter{

	private HashMap<Long,VoiceSender> vc=new HashMap<>();
	public static void main(String[] args){
		String token=Objects.requireNonNull(System.getProperty("token"));
		JDA jda=JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
		jda.addEventListener(new VVTalk());
		jda.updateCommands().addCommands(
			Commands.slash("start_talk", "読み上げを開始します")
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK))
				.setGuildOnly(true)
				.addOption(OptionType.INTEGER, "speaker","声の質を整数値で指定")
				.addOption(OptionType.NUMBER, "speed","読み上げ速度倍率")
				.addOption(OptionType.NUMBER, "pitch","説明文"),
			Commands.slash("stop_talk", "読み上げを停止します")
			.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK))
			.setGuildOnly(true)
		).queue();
		// optionally block until JDA is ready
		try{
			jda.awaitReady();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		System.out.println("bot起動");
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event){
		Guild guild=event.getGuild();
		if(guild==null)return;//DMなど
		long cid=event.getChannel().getIdLong();
		Long gid=guild.getIdLong();
		VoiceSender sender=vc.get(gid);
		if(sender!=null) {
			if(sender.textChannel==cid) {
				String s=toTalkString(event.getMessage());
				sender.insertQueue(s);
			}
			return;
		}
	}
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		Guild guild=event.getGuild();
		if(guild==null)return;//DMなど
		Long gid=guild.getIdLong();
		GuildVoiceState vc_status;
		switch (event.getName()) {
			case "start_talk":
				if(!event.getMember().hasPermission(Permission.VOICE_SPEAK)){
					event.reply("権限不足").setEphemeral(true).queue();
				}else {
					vc_status=event.getMember().getVoiceState();
					if(vc_status!=null) {
						AudioChannelUnion voice_channel=vc_status.getChannel();
						VoiceSender sender=vc.get(gid);
						if(sender!=null) {
							sender.exit();
						}
						sender=new VoiceSender(voice_channel);
						sender.textChannel=event.getChannelIdLong();
						OptionMapping speaker=event.getOption("speaker");
						if(speaker!=null)sender.speaker=speaker.getAsInt();
						OptionMapping speedScale=event.getOption("speed");
						if(speedScale!=null)sender.speedScale=speedScale.getAsDouble();
						OptionMapping pitchScale=event.getOption("pitch");
						if(pitchScale!=null)sender.pitchScale=pitchScale.getAsDouble();
						vc.put(gid,sender);
						System.out.println("speaker"+sender.speaker);
						System.out.println("speedScale"+sender.speedScale);
						System.out.println("pitchScale"+sender.pitchScale);
						event.reply("読み上げ起動").queue();
					}else {
						event.reply("VCに入ってから実行する必要があります").setEphemeral(true).queue();
					}
				}
				break;
			case "stop_talk":
				if(!event.getMember().hasPermission(Permission.VOICE_SPEAK)){
					event.reply("権限不足").setEphemeral(true).queue();
				}else {
					vc_status=event.getMember().getVoiceState();
					VoiceSender sender=vc.remove(gid);
					if(sender!=null) {
						sender.exit();
						event.reply("読み上げ切断").queue();
					}else {
						event.reply("起動してない").setEphemeral(true).queue();//.setEphemeral(true)付けるとコマンド実行者しか見えなくなる
					}
				}
				break;
			default:
				System.out.printf("Unknown command %s used by %#s%n", event.getName(), event.getUser());
		}
	}
	private String toTalkString(Message m) {
		String s=m.getContentDisplay();
		for(var f:m.getAttachments()){
			s+=Normalize.filename(f.getFileName());
		}
		return s;
	}
}
