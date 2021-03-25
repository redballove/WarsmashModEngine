package com.etheller.warsmash.viewer5.handlers.w3x.ui;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.etheller.warsmash.SingleModelScreen;
import com.etheller.warsmash.WarsmashGdxMenuScreen;
import com.etheller.warsmash.WarsmashGdxMultiScreenGame;
import com.etheller.warsmash.datasources.DataSource;
import com.etheller.warsmash.parsers.fdf.GameUI;
import com.etheller.warsmash.parsers.fdf.datamodel.FramePoint;
import com.etheller.warsmash.parsers.fdf.frames.EditBoxFrame;
import com.etheller.warsmash.parsers.fdf.frames.GlueButtonFrame;
import com.etheller.warsmash.parsers.fdf.frames.GlueTextButtonFrame;
import com.etheller.warsmash.parsers.fdf.frames.ListBoxFrame;
import com.etheller.warsmash.parsers.fdf.frames.SetPoint;
import com.etheller.warsmash.parsers.fdf.frames.SimpleFrame;
import com.etheller.warsmash.parsers.fdf.frames.SpriteFrame;
import com.etheller.warsmash.parsers.fdf.frames.StringFrame;
import com.etheller.warsmash.parsers.fdf.frames.UIFrame;
import com.etheller.warsmash.parsers.jass.Jass2.RootFrameListener;
import com.etheller.warsmash.units.DataTable;
import com.etheller.warsmash.units.Element;
import com.etheller.warsmash.units.custom.WTS;
import com.etheller.warsmash.util.StringBundle;
import com.etheller.warsmash.util.WarsmashConstants;
import com.etheller.warsmash.util.WorldEditStrings;
import com.etheller.warsmash.viewer5.Scene;
import com.etheller.warsmash.viewer5.handlers.mdx.MdxViewer;
import com.etheller.warsmash.viewer5.handlers.w3x.UnitSound;
import com.etheller.warsmash.viewer5.handlers.w3x.ui.command.ClickableFrame;
import com.etheller.warsmash.viewer5.handlers.w3x.ui.command.FocusableFrame;
import com.etheller.warsmash.viewer5.handlers.w3x.ui.sound.KeyedSounds;

public class MenuUI {
	private static final Vector2 screenCoordsVector = new Vector2();
	private static boolean ENABLE_NOT_YET_IMPLEMENTED_BUTTONS = false;

	private final DataSource dataSource;
	private final Scene uiScene;
	private final Viewport uiViewport;
	private final MdxViewer viewer;
	private final RootFrameListener rootFrameListener;
	private final float widthRatioCorrection;
	private final float heightRatioCorrection;
	private GameUI rootFrame;
	private SpriteFrame cursorFrame;

	private ClickableFrame mouseDownUIFrame;
	private ClickableFrame mouseOverUIFrame;
	private FocusableFrame focusUIFrame;

	private UIFrame mainMenuFrame;

	private SpriteFrame glueSpriteLayerTopRight;

	private SpriteFrame glueSpriteLayerTopLeft;

	private WorldEditStrings worldEditStrings;

	private DataTable uiSoundsTable;

	private KeyedSounds uiSounds;

	private GlueTextButtonFrame singlePlayerButton;
	private GlueTextButtonFrame battleNetButton;
	private GlueTextButtonFrame localAreaNetworkButton;
	private GlueTextButtonFrame optionsButton;
	private GlueTextButtonFrame creditsButton;
	private GlueButtonFrame realmButton;
	private GlueTextButtonFrame exitButton;

	private final boolean quitting = false;

	private MenuState menuState;

	private UIFrame singlePlayerMenu;
	private UIFrame singlePlayerMainPanel;

	private UIFrame skirmish;

	private UIFrame profilePanel;
	private EditBoxFrame newProfileEditBox;

	private GlueButtonFrame profileButton;
	private GlueTextButtonFrame campaignButton;
	private GlueTextButtonFrame loadSavedButton;
	private GlueTextButtonFrame viewReplayButton;
	private GlueTextButtonFrame customCampaignButton;
	private GlueTextButtonFrame skirmishButton;
	private GlueTextButtonFrame singlePlayerCancelButton;
	private GlueButtonFrame editionButton;

	private GlueTextButtonFrame skirmishCancelButton;

	private final WarsmashGdxMultiScreenGame screenManager;

	private final DataTable warsmashIni;

	private UnitSound glueScreenLoop;

	private SpriteFrame warcraftIIILogo;
	// Campaign
	private UIFrame campaignMenu;
	private SpriteFrame campaignFade;
	private GlueTextButtonFrame campaignBackButton;
	private UIFrame missionSelectFrame;
	private UIFrame campaignSelectFrame;
	private final DataTable campaignStrings;
	private SpriteFrame campaignWarcraftIIILogo;
	private final SingleModelScreen menuScreen;

	private String currentCampaignBackgroundModel;
	private String currentCampaignAmbientSound;
	private int currentCampaignCursor;
	private String[] campaignList;
	private Element[] campaignDatas;
	private UnitSound mainMenuGlueScreenLoop;
	private GlueTextButtonFrame addProfileButton;
	private GlueTextButtonFrame deleteProfileButton;
	private GlueTextButtonFrame selectProfileButton;
	private final PlayerProfileManager profileManager;
	private StringFrame profileNameText;
	private UIFrame confirmDialog;

	public MenuUI(final DataSource dataSource, final Viewport uiViewport, final Scene uiScene, final MdxViewer viewer,
			final WarsmashGdxMultiScreenGame screenManager, final SingleModelScreen menuScreen,
			final DataTable warsmashIni, final RootFrameListener rootFrameListener) {
		this.dataSource = dataSource;
		this.uiViewport = uiViewport;
		this.uiScene = uiScene;
		this.viewer = viewer;
		this.screenManager = screenManager;
		this.menuScreen = menuScreen;
		this.warsmashIni = warsmashIni;
		this.rootFrameListener = rootFrameListener;

		this.widthRatioCorrection = getMinWorldWidth() / 1600f;
		this.heightRatioCorrection = getMinWorldHeight() / 1200f;

		this.campaignStrings = new DataTable(StringBundle.EMPTY);
		try (InputStream campaignStringStream = dataSource.getResourceAsStream(
				"UI\\CampaignStrings" + (WarsmashConstants.GAME_VERSION == 1 ? "_exp" : "") + ".txt")) {
			this.campaignStrings.readTXT(campaignStringStream, true);
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}

		this.profileManager = PlayerProfileManager.loadFromGdx();
	}

	public float getHeightRatioCorrection() {
		return this.heightRatioCorrection;
	}

	/**
	 * Called "main" because this was originally written in JASS so that maps could
	 * override it, and I may convert it back to the JASS at some point.
	 */
	public void main() {
		// =================================
		// Load skins and templates
		// =================================
		this.rootFrame = new GameUI(this.dataSource, GameUI.loadSkin(this.dataSource, WarsmashConstants.GAME_VERSION),
				this.uiViewport, this.uiScene, this.viewer, 0, WTS.DO_NOTHING);

		this.rootFrameListener.onCreate(this.rootFrame);
		try {
			this.rootFrame.loadTOCFile("UI\\FrameDef\\FrameDef.toc");
		}
		catch (final IOException exc) {
			throw new IllegalStateException("Unable to load FrameDef.toc", exc);
		}
		try {
			this.rootFrame.loadTOCFile("UI\\FrameDef\\SmashFrameDef.toc");
		}
		catch (final IOException exc) {
			throw new IllegalStateException("Unable to load SmashFrameDef.toc", exc);
		}

		// Create main menu
		this.mainMenuFrame = this.rootFrame.createFrame("MainMenuFrame", this.rootFrame, 0, 0);

		this.warcraftIIILogo = (SpriteFrame) this.rootFrame.getFrameByName("WarCraftIIILogo", 0);
		this.rootFrame.setSpriteFrameModel(this.warcraftIIILogo,
				this.rootFrame.getSkinField("MainMenuLogo_V" + WarsmashConstants.GAME_VERSION));
		this.warcraftIIILogo.addSetPoint(new SetPoint(FramePoint.TOPLEFT, this.mainMenuFrame, FramePoint.TOPLEFT,
				GameUI.convertX(this.uiViewport, 0.13f), GameUI.convertY(this.uiViewport, -0.08f)));
		setMainMenuVisible(false);
		this.rootFrame.getFrameByName("RealmSelect", 0).setVisible(false);

		this.glueSpriteLayerTopRight = (SpriteFrame) this.rootFrame.createFrameByType("SPRITE",
				"SmashGlueSpriteLayerTopRight", this.rootFrame, "", 0);
		this.glueSpriteLayerTopRight.setSetAllPoints(true);
		final String topRightModel = this.rootFrame
				.getSkinField("GlueSpriteLayerTopRight_V" + WarsmashConstants.GAME_VERSION);
		this.rootFrame.setSpriteFrameModel(this.glueSpriteLayerTopRight, topRightModel);
		this.glueSpriteLayerTopRight.setSequence("MainMenu Birth");

		this.glueSpriteLayerTopLeft = (SpriteFrame) this.rootFrame.createFrameByType("SPRITE",
				"SmashGlueSpriteLayerTopLeft", this.rootFrame, "", 0);
		this.glueSpriteLayerTopLeft.setSetAllPoints(true);
		final String topLeftModel = this.rootFrame
				.getSkinField("GlueSpriteLayerTopLeft_V" + WarsmashConstants.GAME_VERSION);
		this.rootFrame.setSpriteFrameModel(this.glueSpriteLayerTopLeft, topLeftModel);
		this.glueSpriteLayerTopLeft.setSequence("MainMenu Birth");

		this.cursorFrame = (SpriteFrame) this.rootFrame.createFrameByType("SPRITE", "SmashCursorFrame", this.rootFrame,
				"", 0);
		this.rootFrame.setSpriteFrameModel(this.cursorFrame, this.rootFrame.getSkinField("Cursor"));
		this.cursorFrame.setSequence("Normal");
		this.cursorFrame.setZDepth(-1.0f);
		Gdx.input.setCursorCatched(true);

		// Main Menu interactivity
		this.singlePlayerButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("SinglePlayerButton", 0);
		this.battleNetButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("BattleNetButton", 0);
		this.realmButton = (GlueButtonFrame) this.rootFrame.getFrameByName("RealmButton", 0);
		this.localAreaNetworkButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("LocalAreaNetworkButton", 0);
		this.optionsButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("OptionsButton", 0);
		this.creditsButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("CreditsButton", 0);
		this.exitButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("ExitButton", 0);
		this.editionButton = (GlueButtonFrame) this.rootFrame.getFrameByName("EditionButton", 0);

		if (this.editionButton != null) {
			this.editionButton.setOnClick(new Runnable() {
				@Override
				public void run() {
					WarsmashConstants.GAME_VERSION = (WarsmashConstants.GAME_VERSION == 1 ? 0 : 1);
					MenuUI.this.glueSpriteLayerTopLeft.setSequence("MainMenu Death");
					MenuUI.this.glueSpriteLayerTopRight.setSequence("MainMenu Death");
					setMainMenuVisible(false);
					MenuUI.this.menuState = MenuState.RESTARTING;
				}
			});
		}

		this.battleNetButton.setEnabled(false);
		this.realmButton.setEnabled(false);
		this.localAreaNetworkButton.setEnabled(false);
		this.optionsButton.setEnabled(false);
		this.creditsButton.setEnabled(false);

		this.exitButton.setOnClick(new Runnable() {
			@Override
			public void run() {
				MenuUI.this.glueSpriteLayerTopLeft.setSequence("MainMenu Death");
				MenuUI.this.glueSpriteLayerTopRight.setSequence("MainMenu Death");
				setMainMenuVisible(false);
				MenuUI.this.menuState = MenuState.QUITTING;
			}
		});

		this.singlePlayerButton.setOnClick(new Runnable() {
			@Override
			public void run() {
				MenuUI.this.glueSpriteLayerTopLeft.setSequence("MainMenu Death");
				MenuUI.this.glueSpriteLayerTopRight.setSequence("MainMenu Death");
				setMainMenuVisible(false);
				MenuUI.this.menuState = MenuState.GOING_TO_SINGLE_PLAYER;
			}
		});

		// Create single player
		this.singlePlayerMenu = this.rootFrame.createFrame("SinglePlayerMenu", this.rootFrame, 0, 0);
		this.singlePlayerMenu.setVisible(false);

		this.profilePanel = this.rootFrame.getFrameByName("ProfilePanel", 0);
		this.profilePanel.setVisible(false);

		this.newProfileEditBox = (EditBoxFrame) this.rootFrame.getFrameByName("NewProfileEditBox", 0);
		this.newProfileEditBox.setOnChange(new Runnable() {
			@Override
			public void run() {
				MenuUI.this.addProfileButton
						.setEnabled(!MenuUI.this.profileManager.hasProfile(MenuUI.this.newProfileEditBox.getText()));
			}
		});
		final StringFrame profileListText = (StringFrame) this.rootFrame.getFrameByName("ProfileListText", 0);
		final SimpleFrame profileListContainer = (SimpleFrame) this.rootFrame.getFrameByName("ProfileListContainer", 0);
		final ListBoxFrame profileListBox = (ListBoxFrame) this.rootFrame.createFrameByType("LISTBOX", "ListBoxWar3",
				profileListContainer, "WITHCHILDREN", 0);
		profileListBox.setSetAllPoints(true);
		profileListBox.setFrameFont(profileListText.getFrameFont());
		for (final PlayerProfile profile : this.profileManager.getProfiles()) {
			profileListBox.addItem(profile.getName(), this.rootFrame, this.uiViewport);
		}
		profileListContainer.add(profileListBox);

		this.addProfileButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("AddProfileButton", 0);
		this.deleteProfileButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("DeleteProfileButton", 0);
		this.selectProfileButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("SelectProfileButton", 0);
		this.selectProfileButton.setEnabled(false);
		this.deleteProfileButton.setEnabled(false);
		this.addProfileButton.setOnClick(new Runnable() {
			@Override
			public void run() {
				final String newProfileName = MenuUI.this.newProfileEditBox.getText();
				if (!newProfileName.isEmpty() && !MenuUI.this.profileManager.hasProfile(newProfileName)) {
					MenuUI.this.profileManager.addProfile(newProfileName);
					profileListBox.addItem(newProfileName, MenuUI.this.rootFrame, MenuUI.this.uiViewport);
					MenuUI.this.addProfileButton.setEnabled(false);
				}
			}
		});
		this.deleteProfileButton.setOnClick(new Runnable() {
			@Override
			public void run() {
				final int selectedIndex = profileListBox.getSelectedIndex();
				final boolean validSelect = (selectedIndex >= 0)
						&& (selectedIndex < MenuUI.this.profileManager.getProfiles().size());
				if (validSelect) {
					if (MenuUI.this.profileManager.getProfiles().size() > 1) {
						final PlayerProfile profileToRemove = MenuUI.this.profileManager.getProfiles()
								.get(selectedIndex);
						final String removeProfileName = profileToRemove.getName();
						final boolean deletingCurrentProfile = removeProfileName
								.equals(MenuUI.this.profileManager.getCurrentProfile());
						MenuUI.this.profileManager.removeProfile(profileToRemove);
						profileListBox.removeItem(selectedIndex, MenuUI.this.rootFrame, MenuUI.this.uiViewport);
						if (deletingCurrentProfile) {
							setCurrentProfile(MenuUI.this.profileManager.getProfiles().get(0).getName());
						}
					}
				}
			}
		});
		this.selectProfileButton.setOnClick(new Runnable() {
			@Override
			public void run() {
				final int selectedIndex = profileListBox.getSelectedIndex();
				final boolean validSelect = (selectedIndex >= 0)
						&& (selectedIndex < MenuUI.this.profileManager.getProfiles().size());
				if (validSelect) {
					final PlayerProfile profileToSelect = MenuUI.this.profileManager.getProfiles().get(selectedIndex);
					final String selectedProfileName = profileToSelect.getName();
					setCurrentProfile(selectedProfileName);

					MenuUI.this.glueSpriteLayerTopLeft.setSequence("RealmSelection Death");
					MenuUI.this.profilePanel.setVisible(false);
					MenuUI.this.menuState = MenuState.SINGLE_PLAYER;
					setSinglePlayerButtonsEnabled(false);
				}

			}

		});
		profileListBox.setOnSelect(new Runnable() {
			@Override
			public void run() {
				final int selectedIndex = profileListBox.getSelectedIndex();
				final boolean validSelect = (selectedIndex >= 0)
						&& (selectedIndex < MenuUI.this.profileManager.getProfiles().size());
				MenuUI.this.selectProfileButton.setEnabled(validSelect);
				MenuUI.this.deleteProfileButton.setEnabled(validSelect);
			}
		});

		this.singlePlayerMainPanel = this.rootFrame.getFrameByName("MainPanel", 0);

		// Single Player Interactivity
		this.profileButton = (GlueButtonFrame) this.rootFrame.getFrameByName("ProfileButton", 0);
		this.campaignButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("CampaignButton", 0);
		this.loadSavedButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("LoadSavedButton", 0);
		this.viewReplayButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("ViewReplayButton", 0);
		this.customCampaignButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("CustomCampaignButton", 0);
		this.skirmishButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("SkirmishButton", 0);

		this.singlePlayerCancelButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("CancelButton", 0);

		this.profileNameText = (StringFrame) this.rootFrame.getFrameByName("ProfileNameText", 0);
		this.rootFrame.setText(this.profileNameText, this.profileManager.getCurrentProfile());

		setSinglePlayerButtonsEnabled(true);

		this.profileButton.setOnClick(new Runnable() {
			@Override
			public void run() {
				MenuUI.this.glueSpriteLayerTopLeft.setSequence("RealmSelection Birth");
				setSinglePlayerButtonsEnabled(false);
				MenuUI.this.menuState = MenuState.SINGLE_PLAYER_PROFILE;
			}
		});

		this.campaignButton.setOnClick(new Runnable() {
			@Override
			public void run() {
				MenuUI.this.glueSpriteLayerTopLeft.setSequence("SinglePlayer Death");
				MenuUI.this.glueSpriteLayerTopRight.setSequence("SinglePlayer Death");
				MenuUI.this.singlePlayerMenu.setVisible(false);
				MenuUI.this.profilePanel.setVisible(false);
				MenuUI.this.menuState = MenuState.GOING_TO_CAMPAIGN;
			}
		});

		this.skirmishButton.setOnClick(new Runnable() {
			@Override
			public void run() {
				MenuUI.this.glueSpriteLayerTopLeft.setSequence("SinglePlayer Death");
				MenuUI.this.glueSpriteLayerTopRight.setSequence("SinglePlayer Death");
				MenuUI.this.singlePlayerMenu.setVisible(false);
				MenuUI.this.profilePanel.setVisible(false);
				MenuUI.this.menuState = MenuState.GOING_TO_SINGLE_PLAYER_SKIRMISH;
			}
		});

		this.singlePlayerCancelButton.setOnClick(new Runnable() {
			@Override
			public void run() {
				if (MenuUI.this.menuState == MenuState.SINGLE_PLAYER_PROFILE) {
					MenuUI.this.glueSpriteLayerTopLeft.setSequence("RealmSelection Death");
					MenuUI.this.profilePanel.setVisible(false);
				}
				else {
					MenuUI.this.glueSpriteLayerTopLeft.setSequence("SinglePlayer Death");
				}
				MenuUI.this.glueSpriteLayerTopRight.setSequence("SinglePlayer Death");
				MenuUI.this.singlePlayerMenu.setVisible(false);
				MenuUI.this.menuState = MenuState.GOING_TO_MAIN_MENU;
			}
		});

		// Create skirmish UI
		this.skirmish = this.rootFrame.createFrame("Skirmish", this.rootFrame, 0, 0);
		this.skirmish.setVisible(false);

		this.skirmishCancelButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("CancelButton", 0);
		this.skirmishCancelButton.setOnClick(new Runnable() {
			@Override
			public void run() {
				MenuUI.this.glueSpriteLayerTopLeft.setSequence("SinglePlayerSkirmish Death");
				MenuUI.this.glueSpriteLayerTopRight.setSequence("SinglePlayerSkirmish Death");
				MenuUI.this.skirmish.setVisible(false);
				MenuUI.this.menuState = MenuState.GOING_TO_SINGLE_PLAYER;

			}
		});

		// Create Campaign UI

		this.campaignMenu = this.rootFrame.createFrame("CampaignMenu", this.rootFrame, 0, 0);
		this.campaignMenu.setVisible(false);
		this.campaignFade = (SpriteFrame) this.rootFrame.getFrameByName("SlidingDoors", 0);
		this.campaignFade.setVisible(false);
		this.campaignBackButton = (GlueTextButtonFrame) this.rootFrame.getFrameByName("BackButton", 0);
		this.campaignBackButton.setVisible(false);
		this.missionSelectFrame = this.rootFrame.getFrameByName("MissionSelectFrame", 0);
		this.missionSelectFrame.setVisible(false);
		this.campaignSelectFrame = this.rootFrame.getFrameByName("CampaignSelectFrame", 0);
		this.campaignSelectFrame.setVisible(false);

		this.campaignWarcraftIIILogo = (SpriteFrame) this.rootFrame.getFrameByName("WarCraftIIILogo", 0);
		this.rootFrame.setSpriteFrameModel(this.campaignWarcraftIIILogo,
				this.rootFrame.getSkinField("MainMenuLogo_V" + WarsmashConstants.GAME_VERSION));
		this.campaignWarcraftIIILogo.setVisible(false);

		this.campaignBackButton.setOnClick(new Runnable() {
			@Override
			public void run() {
				MenuUI.this.campaignMenu.setVisible(false);
				MenuUI.this.campaignBackButton.setVisible(false);
				MenuUI.this.missionSelectFrame.setVisible(false);
				MenuUI.this.campaignSelectFrame.setVisible(false);
				MenuUI.this.campaignFade.setSequence("Birth");
				MenuUI.this.menuState = MenuState.LEAVING_CAMPAIGN;
			}
		});
		final Element campaignIndex = this.campaignStrings.get("Index");
		this.campaignList = campaignIndex.getField("CampaignList").split(",");
		this.campaignDatas = new Element[this.campaignList.length];
		for (int i = 0; i < this.campaignList.length; i++) {
			final String campaign = this.campaignList[i];
			this.campaignDatas[i] = this.campaignStrings.get(campaign);
			if ((this.campaignDatas[i] != null) && (this.currentCampaignBackgroundModel == null)) {
				this.currentCampaignBackgroundModel = this.rootFrame.getSkinField(
						this.campaignDatas[i].getField("Background") + "_V" + WarsmashConstants.GAME_VERSION);
				this.currentCampaignAmbientSound = this.rootFrame
						.trySkinField(this.campaignDatas[i].getField("AmbientSound"));
				this.currentCampaignCursor = this.campaignDatas[i].getFieldValue("Cursor");
			}
		}

		this.confirmDialog = this.rootFrame.createFrame("DialogWar3", this.rootFrame, 0, 0);
		this.confirmDialog.setVisible(false);

		// position all
		this.rootFrame.positionBounds(this.rootFrame, this.uiViewport);

		this.menuState = MenuState.GOING_TO_MAIN_MENU;

		loadSounds();

		final String glueLoopField = this.rootFrame.getSkinField("GlueScreenLoop_V" + WarsmashConstants.GAME_VERSION);
		this.mainMenuGlueScreenLoop = this.uiSounds.getSound(glueLoopField);
		this.glueScreenLoop = this.mainMenuGlueScreenLoop;
		this.glueScreenLoop.play(this.uiScene.audioContext, 0f, 0f, 0f);
	}

	private void setCurrentProfile(final String selectedProfileName) {
		this.profileManager.setCurrentProfile(selectedProfileName);
		this.rootFrame.setText(MenuUI.this.profileNameText, selectedProfileName);
	}

	protected void setSinglePlayerButtonsEnabled(final boolean b) {
		this.profileButton.setEnabled(b);
		this.campaignButton.setEnabled(b);
		this.loadSavedButton.setEnabled(b && ENABLE_NOT_YET_IMPLEMENTED_BUTTONS);
		this.viewReplayButton.setEnabled(b && ENABLE_NOT_YET_IMPLEMENTED_BUTTONS);
		this.customCampaignButton.setEnabled(b && ENABLE_NOT_YET_IMPLEMENTED_BUTTONS);
		this.skirmishButton.setEnabled(b);
		this.singlePlayerCancelButton.setEnabled(b);
	}

	private void setMainMenuVisible(final boolean visible) {
		this.mainMenuFrame.setVisible(visible);
		this.warcraftIIILogo.setVisible(visible);
	}

	public void resize() {

	}

	public void render(final SpriteBatch batch, final GlyphLayout glyphLayout) {
		final BitmapFont font = this.rootFrame.getFont();
		final BitmapFont font20 = this.rootFrame.getFont20();
		font.setColor(Color.YELLOW);
		final String fpsString = "FPS: " + Gdx.graphics.getFramesPerSecond();
		glyphLayout.setText(font, fpsString);
		font.draw(batch, fpsString, (getMinWorldWidth() - glyphLayout.width) / 2, 1100 * this.heightRatioCorrection);
		this.rootFrame.render(batch, font20, glyphLayout);
	}

	private float getMinWorldWidth() {
		if (this.uiViewport instanceof ExtendViewport) {
			return ((ExtendViewport) this.uiViewport).getMinWorldWidth();
		}
		return this.uiViewport.getWorldWidth();
	}

	private float getMinWorldHeight() {
		if (this.uiViewport instanceof ExtendViewport) {
			return ((ExtendViewport) this.uiViewport).getMinWorldHeight();
		}
		return this.uiViewport.getWorldHeight();
	}

	public void update(final float deltaTime) {
		if ((this.focusUIFrame != null) && !this.focusUIFrame.isVisibleOnScreen()) {
			setFocusFrame(getNextFocusFrame());
		}

		final int baseMouseX = Gdx.input.getX();
		int mouseX = baseMouseX;
		final int baseMouseY = Gdx.input.getY();
		int mouseY = baseMouseY;
		final int minX = this.uiViewport.getScreenX();
		final int maxX = minX + this.uiViewport.getScreenWidth();
		final int minY = this.uiViewport.getScreenY();
		final int maxY = minY + this.uiViewport.getScreenHeight();

		mouseX = Math.max(minX, Math.min(maxX, mouseX));
		mouseY = Math.max(minY, Math.min(maxY, mouseY));
		if (Gdx.input.isCursorCatched()) {
			Gdx.input.setCursorPosition(mouseX, mouseY);
		}

		screenCoordsVector.set(mouseX, mouseY);
		this.uiViewport.unproject(screenCoordsVector);
		this.cursorFrame.setFramePointX(FramePoint.LEFT, screenCoordsVector.x);
		this.cursorFrame.setFramePointY(FramePoint.BOTTOM, screenCoordsVector.y);
		this.cursorFrame.setSequence("Normal");

		if (this.glueSpriteLayerTopRight.isSequenceEnded() && this.glueSpriteLayerTopLeft.isSequenceEnded()
				&& (!this.campaignFade.isVisible() || this.campaignFade.isSequenceEnded())) {
			switch (this.menuState) {
			case GOING_TO_MAIN_MENU:
				this.glueSpriteLayerTopLeft.setSequence("MainMenu Birth");
				this.glueSpriteLayerTopRight.setSequence("MainMenu Birth");
				this.menuState = MenuState.MAIN_MENU;
				break;
			case MAIN_MENU:
				setMainMenuVisible(true);
				this.glueSpriteLayerTopLeft.setSequence("MainMenu Stand");
				this.glueSpriteLayerTopRight.setSequence("MainMenu Stand");
				break;
			case GOING_TO_SINGLE_PLAYER:
				this.glueSpriteLayerTopLeft.setSequence("SinglePlayer Birth");
				this.glueSpriteLayerTopRight.setSequence("SinglePlayer Birth");
				this.menuState = MenuState.SINGLE_PLAYER;
				break;
			case LEAVING_CAMPAIGN:
				this.glueSpriteLayerTopLeft.setSequence("Birth");
				this.glueSpriteLayerTopRight.setSequence("Birth");
				if (this.campaignFade.isVisible()) {
					this.campaignFade.setSequence("Death");
				}
				this.glueScreenLoop.stop();
				this.glueScreenLoop = this.mainMenuGlueScreenLoop;
				this.glueScreenLoop.play(this.uiScene.audioContext, 0f, 0f, 0f);
				this.menuScreen.setModel(
						this.rootFrame.getSkinField("GlueSpriteLayerBackground_V" + WarsmashConstants.GAME_VERSION));
				this.rootFrame.setSpriteFrameModel(this.cursorFrame, this.rootFrame.getSkinField("Cursor"));
				this.menuState = MenuState.GOING_TO_SINGLE_PLAYER;
				break;
			case SINGLE_PLAYER:
				this.singlePlayerMenu.setVisible(true);
				this.campaignFade.setVisible(false);
				setSinglePlayerButtonsEnabled(true);
				this.glueSpriteLayerTopLeft.setSequence("SinglePlayer Stand");
				this.glueSpriteLayerTopRight.setSequence("SinglePlayer Stand");
				break;
			case GOING_TO_SINGLE_PLAYER_SKIRMISH:
				this.glueSpriteLayerTopLeft.setSequence("SinglePlayerSkirmish Birth");
				this.glueSpriteLayerTopRight.setSequence("SinglePlayerSkirmish Birth");
				this.menuState = MenuState.SINGLE_PLAYER_SKIRMISH;
				break;
			case SINGLE_PLAYER_SKIRMISH:
				this.skirmish.setVisible(true);
				this.glueSpriteLayerTopLeft.setSequence("SinglePlayerSkirmish Stand");
				this.glueSpriteLayerTopRight.setSequence("SinglePlayerSkirmish Stand");
				break;
			case GOING_TO_CAMPAIGN:
				this.glueSpriteLayerTopLeft.setSequence("Death");
				this.glueSpriteLayerTopRight.setSequence("Death");
				this.campaignMenu.setVisible(true);
				this.campaignFade.setVisible(true);
				this.campaignFade.setSequence("Birth");
				this.menuState = MenuState.GOING_TO_CAMPAIGN_PART2;
				break;
			case GOING_TO_CAMPAIGN_PART2:
				this.menuScreen.setModel(this.currentCampaignBackgroundModel);
				this.glueScreenLoop.stop();
				this.glueScreenLoop = this.uiSounds.getSound(this.currentCampaignAmbientSound);
				this.glueScreenLoop.play(this.uiScene.audioContext, 0f, 0f, 0f);
				final DataTable skinData = this.rootFrame.getSkinData();
				final Element skinDataMain = skinData.get("Main");
				int currentCampaignCursor = this.currentCampaignCursor;
				if (currentCampaignCursor == 3) {
					currentCampaignCursor = 2;
				}
				else if (currentCampaignCursor == 2) {
					currentCampaignCursor = 3;
				}
				final String cursorSkin = skinDataMain.getField("Skins", currentCampaignCursor);
				this.rootFrame.setSpriteFrameModel(this.cursorFrame, skinData.get(cursorSkin).getField("Cursor"));

				this.campaignFade.setSequence("Death");
				this.menuState = MenuState.CAMPAIGN;
				break;
			case CAMPAIGN:
				this.campaignBackButton.setVisible(true);
				this.campaignWarcraftIIILogo.setVisible(true);
				this.campaignSelectFrame.setVisible(true);
				break;
			case GOING_TO_SINGLE_PLAYER_PROFILE:
				this.glueSpriteLayerTopLeft.setSequence("RealmSelection Birth");
				this.menuState = MenuState.SINGLE_PLAYER_PROFILE;
				break;
			case SINGLE_PLAYER_PROFILE:
				this.profilePanel.setVisible(true);
				setSinglePlayerButtonsEnabled(true);
				this.glueSpriteLayerTopLeft.setSequence("RealmSelection Stand");
				// TODO the below should probably be some generic focusing thing when we enter a
				// new view?
				if ((this.newProfileEditBox != null) && this.newProfileEditBox.isFocusable()) {
					setFocusFrame(this.newProfileEditBox);
				}
				break;
			case QUITTING:
				Gdx.app.exit();
				break;
			case RESTARTING:
				MenuUI.this.screenManager
						.setScreen(new WarsmashGdxMenuScreen(MenuUI.this.warsmashIni, this.screenManager));
				break;
			default:
				break;
			}
		}

	}

	private FocusableFrame getNextFocusFrame() {
		return this.rootFrame.getNextFocusFrame();
	}

	public boolean touchDown(final int screenX, final int screenY, final float worldScreenY, final int button) {
		screenCoordsVector.set(screenX, screenY);
		this.uiViewport.unproject(screenCoordsVector);
		final UIFrame clickedUIFrame = this.rootFrame.touchDown(screenCoordsVector.x, screenCoordsVector.y, button);
		if (clickedUIFrame != null) {
			if (clickedUIFrame instanceof ClickableFrame) {
				this.mouseDownUIFrame = (ClickableFrame) clickedUIFrame;
				this.mouseDownUIFrame.mouseDown(this.rootFrame, this.uiViewport);
			}
			if (clickedUIFrame instanceof FocusableFrame) {
				final FocusableFrame clickedFocusableFrame = (FocusableFrame) clickedUIFrame;
				if (clickedFocusableFrame.isFocusable()) {
					setFocusFrame(clickedFocusableFrame);
				}
			}
		}
		return false;
	}

	private void setFocusFrame(final FocusableFrame clickedFocusableFrame) {
		if (this.focusUIFrame != null) {
			this.focusUIFrame.onFocusLost();
		}
		this.focusUIFrame = clickedFocusableFrame;
		if (this.focusUIFrame != null) {
			this.focusUIFrame.onFocusGained();
		}
	}

	public boolean touchUp(final int screenX, final int screenY, final float worldScreenY, final int button) {
		screenCoordsVector.set(screenX, screenY);
		this.uiViewport.unproject(screenCoordsVector);
		final UIFrame clickedUIFrame = this.rootFrame.touchUp(screenCoordsVector.x, screenCoordsVector.y, button);
		if (this.mouseDownUIFrame != null) {
			if (clickedUIFrame == this.mouseDownUIFrame) {
				this.mouseDownUIFrame.onClick(button);
				this.uiSounds.getSound("GlueScreenClick").play(this.uiScene.audioContext, 0, 0, 0);
			}
			this.mouseDownUIFrame.mouseUp(this.rootFrame, this.uiViewport);
		}
		this.mouseDownUIFrame = null;
		return false;
	}

	public boolean touchDragged(final int screenX, final int screenY, final float worldScreenY, final int pointer) {
		mouseMoved(screenX, screenY, worldScreenY);
		return false;
	}

	public boolean mouseMoved(final int screenX, final int screenY, final float worldScreenY) {
		screenCoordsVector.set(screenX, screenY);
		this.uiViewport.unproject(screenCoordsVector);
		final UIFrame mousedUIFrame = this.rootFrame.getFrameChildUnderMouse(screenCoordsVector.x,
				screenCoordsVector.y);
		if (mousedUIFrame != this.mouseOverUIFrame) {
			if (this.mouseOverUIFrame != null) {
				this.mouseOverUIFrame.mouseExit(this.rootFrame, this.uiViewport);
			}
			if (mousedUIFrame instanceof ClickableFrame) {
				this.mouseOverUIFrame = (ClickableFrame) mousedUIFrame;
				if (this.mouseOverUIFrame != null) {
					this.mouseOverUIFrame.mouseEnter(this.rootFrame, this.uiViewport);
				}
			}
			else {
				this.mouseOverUIFrame = null;
			}
		}
		return false;
	}

	private void loadSounds() {
		this.worldEditStrings = new WorldEditStrings(this.dataSource);
		this.uiSoundsTable = new DataTable(this.worldEditStrings);
		try {
			try (InputStream miscDataTxtStream = this.dataSource.getResourceAsStream("UI\\SoundInfo\\UISounds.slk")) {
				this.uiSoundsTable.readSLK(miscDataTxtStream);
			}
			try (InputStream miscDataTxtStream = this.dataSource
					.getResourceAsStream("UI\\SoundInfo\\AmbienceSounds.slk")) {
				this.uiSoundsTable.readSLK(miscDataTxtStream);
			}
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
		this.uiSounds = new KeyedSounds(this.uiSoundsTable, this.dataSource);
	}

	public KeyedSounds getUiSounds() {
		return this.uiSounds;
	}

	private static enum MenuState {
		GOING_TO_MAIN_MENU,
		MAIN_MENU,
		GOING_TO_SINGLE_PLAYER,
		LEAVING_CAMPAIGN,
		SINGLE_PLAYER,
		GOING_TO_SINGLE_PLAYER_SKIRMISH,
		SINGLE_PLAYER_SKIRMISH,
		GOING_TO_CAMPAIGN,
		GOING_TO_CAMPAIGN_PART2,
		CAMPAIGN,
		GOING_TO_SINGLE_PLAYER_PROFILE,
		SINGLE_PLAYER_PROFILE,
		QUITTING,
		RESTARTING;
	}

	public void hide() {
		this.glueScreenLoop.stop();
	}

	public void dispose() {
		if (this.rootFrame != null) {
			this.rootFrame.dispose();
		}
	}

	public boolean keyDown(final int keycode) {
		if (this.focusUIFrame != null) {
			this.focusUIFrame.keyDown(keycode);
		}
		return false;
	}

	public boolean keyUp(final int keycode) {
		if (this.focusUIFrame != null) {
			this.focusUIFrame.keyUp(keycode);
		}
		return false;
	}

	public boolean keyTyped(final char character) {
		if (this.focusUIFrame != null) {
			this.focusUIFrame.keyTyped(character);
		}
		return false;
	}
}
