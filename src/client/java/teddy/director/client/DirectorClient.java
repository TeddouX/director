package teddy.director.client;

import com.mojang.brigadier.CommandDispatcher;
import foundry.imgui.fabric.api.event.ImGuiLoadEventsFabric;
import foundry.imgui.fabric.api.event.RenderImGuiEventsFabric;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import teddy.director.client.data.DummyData;
import teddy.director.client.entity.ClientEntities;
import teddy.director.client.entity.DummyEntity;
import teddy.director.client.entity.rendering.DummyEntityRenderer;
import teddy.director.client.playback.ScenePlaybackController;
import teddy.director.client.screen.DirectorScreen;

import java.util.UUID;

public class DirectorClient implements ClientModInitializer {
	private static boolean openTimelineScreen = false;
	private static int nextId = Integer.MIN_VALUE / 2;

	public static int nextClientID() {
		return nextId++;
	}

	@Override
	public void onInitializeClient() {
        ClientEntities.registerAll();
		DirectorKeybinds.registerKeyMappings();

		registerEntityRenderers();
		registerEventCallbacks();
		registerImGuiEvents();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> registerCommands(dispatcher));
	}

	private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("director").executes(_ -> {
			if (!DirectorState.IS_ACTIVE)
				return 0;

			openTimelineScreen = true;

			return 1;
		}));

		dispatcher.register(ClientCommands.literal("director_dummy").executes(ctx -> {
			if (DirectorState.CURRENT_MOVIE == null) {
				ctx.getSource().sendFeedback(Component.literal("No movie active. Please select a movie before summoning dummies."));
				return 0;
			}

			if (DirectorState.CURRENT_SCENE == null) {
				ctx.getSource().sendFeedback(Component.literal("No scene selected. Please select a scene before summoning dummies."));
				return 0;
			}

			ClientLevel level = ctx.getSource().getLevel();
			Vec3 pos = ctx.getSource().getPosition();

			DummyEntity entity = new DummyEntity(ClientEntities.DUMMY, level);
			entity.setPos(pos);
			entity.setId(nextClientID());

			DummyData data = new DummyData();
			data.displayEntityID = BuiltInRegistries.ENTITY_TYPE.getKey(DirectorState.DEFAULT_DUMMY_ENTITY_TYPE).toString();
			data.name = "New Dummy";
			data.uuid = UUID.randomUUID();

			entity.data = data;
			entity.displayEntityType = DirectorState.DEFAULT_DUMMY_ENTITY_TYPE;

			DirectorState.CURRENT_SCENE.dummies.add(data);

			level.addEntity(entity);

			ScenePlaybackController.addDummyEntity(entity);

			return 1;
		}));
	}

	private void registerEntityRenderers() {
		EntityRenderers.register(ClientEntities.DUMMY, DummyEntityRenderer::new);
	}

	private void registerEventCallbacks() {
		// The mod is only active in singleplayer
		ClientPlayConnectionEvents.JOIN.register((_, _, client) ->
			DirectorState.IS_ACTIVE = client.hasSingleplayerServer()
		);

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (openTimelineScreen) {
				openTimelineScreen = false;
				mc.gui.setScreen(new DirectorScreen(Component.empty()));
			}

			if (DirectorKeybinds.openDirectorUIKey.isDown() && !DirectorScreen.ACTIVE)
				mc.gui.setScreen(new DirectorScreen(Component.empty()));
		});
	}

	private void registerImGuiEvents() {
		ImGuiLoadEventsFabric.POST.register(() -> {
			ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable);
		});

		RenderImGuiEventsFabric.POST.register(() -> {
			ImGui.dockSpaceOverViewport(0, ImGui.getMainViewport(), ImGuiDockNodeFlags.PassthruCentralNode);

			DirectorScreen.renderImGui();
		});
	}
}