package teddy.director.client;

import com.mojang.brigadier.CommandDispatcher;
import foundry.imgui.fabric.api.event.ImGuiLoadEventsFabric;
import foundry.imgui.fabric.api.event.RenderImGuiEventsFabric;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.network.chat.Component;
import teddy.director.client.entity.ClientEntities;
import teddy.director.client.entity.rendering.DummyEntityRenderer;
import teddy.director.client.rendering.RenderCamera;
import teddy.director.client.screen.DirectorScreen;

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
			if (!DirectorState.isActive)
				return 0;

			openTimelineScreen = true;

			return 1;
		}));
	}

	private void registerEntityRenderers() {
		EntityRenderers.register(ClientEntities.DUMMY, DummyEntityRenderer::new);
	}

	private void registerEventCallbacks() {
		// The mod is only active in singleplayer
		ClientPlayConnectionEvents.JOIN.register((_, _, client) -> {
				boolean isSinglePlayer = client.hasSingleplayerServer();
				DirectorState.isActive = isSinglePlayer;

				if (isSinglePlayer)
					DirectorState.renderCamera = new RenderCamera();
			}
		);

		ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> {
			DirectorState.reset();

			DirectorState.renderCamera.close();
			DirectorState.renderCamera = null;
		});

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (!DirectorState.isActive)
				return;

			if (openTimelineScreen) {
				openTimelineScreen = false;
				mc.gui.setScreen(new DirectorScreen(Component.empty()));
			}

			if (DirectorKeybinds.openDirectorUIKey.isDown() && !DirectorScreen.active)
				mc.gui.setScreen(new DirectorScreen(Component.empty()));
		});
	}

	private void registerImGuiEvents() {
		ImGuiLoadEventsFabric.POST.register(() -> ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable));

		RenderImGuiEventsFabric.PRE.register(() -> {
			ImVec2 size = ImGui.getIO().getDisplaySize();

			ImGuizmo.beginFrame();
			ImGuizmo.setOrthographic(false);
			ImGuizmo.setDrawList(ImGui.getBackgroundDrawList());
			ImGuizmo.setRect(0, 0, size.x, size.y);
		});

		RenderImGuiEventsFabric.POST.register(() -> {
			ImGui.dockSpaceOverViewport(0, ImGui.getMainViewport(), ImGuiDockNodeFlags.PassthruCentralNode);

			DirectorScreen.renderImGui();
		});
	}
}