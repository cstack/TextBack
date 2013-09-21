#include "pebble_os.h"
#include "pebble_app.h"
#include "pebble_fonts.h"


/*
UUID E8AE10A2-2E91-473E-B2FA6DD382BACD52
*/
#define MY_UUID { 0xE8, 0xAE, 0x10, 0xA2, 0x2E, 0x91, 0x47, 0x3E, 0xB2, 0xFA, 0x6D, 0xD3, 0x82, 0xBA, 0xCD, 0x52 }
PBL_APP_INFO(MY_UUID,
             "TextBack", "MHacks",
             1, 0, /* App version */
             DEFAULT_MENU_ICON,
             APP_INFO_STANDARD_APP);


#define NUM_MENU_ITEMS 3
#define NUM_MENU_SECTIONS 1

#define MESSAGE_KEY 0x0

static void app_send_succeeded(DictionaryIterator *sent, void *context);
static void app_send_failed(DictionaryIterator* failed, AppMessageResult reason, void* context);
static void app_received_msg(DictionaryIterator* received, void* context);
void hide_message_view();


Window window;

// Notification
ScrollLayer scroll_layer;
TextLayer text_layer;
char scroll_text[256];
const int vert_scroll_text_padding = 10;

// Response Menu
SimpleMenuLayer simple_menu_layer;
SimpleMenuSection menu_sections[NUM_MENU_SECTIONS];
SimpleMenuItem menu_items[NUM_MENU_ITEMS];

static bool callbacks_registered;
static AppMessageCallbacksNode app_callbacks;

int selected_message;

bool register_callbacks() {
  if (callbacks_registered) {
    if (app_message_deregister_callbacks(&app_callbacks) == APP_MSG_OK)
      callbacks_registered = false;
  }
  if (!callbacks_registered) {
    app_callbacks = (AppMessageCallbacksNode){
      .callbacks = {
        .out_sent = app_send_succeeded,
        .out_failed = app_send_failed,
        .in_received = app_received_msg
      },
      .context = NULL
    };
    if (app_message_register_callbacks(&app_callbacks) == APP_MSG_OK) {
      callbacks_registered = true;
    }
  }
  return callbacks_registered;
}

void send_message(const char * message) {
  Tuplet value = TupletCString(MESSAGE_KEY, message);

  DictionaryIterator *iter;
  app_message_out_get(&iter);

  if (iter == NULL)
    return;

  dict_write_tuplet(iter, &value);
  dict_write_end(iter);

  app_message_out_send();
  app_message_out_release();
}

void display_message(const char * message) {
  strcpy(scroll_text, message);

  // Trim text layer and scroll content to fit text box
  GSize max_size = text_layer_get_max_used_size(app_get_current_graphics_context(), &text_layer);
  scroll_layer_set_content_size(&scroll_layer, GSize(144, max_size.h + vert_scroll_text_padding));

  layer_mark_dirty(&(text_layer.layer));
}

// You can capture when the user selects a menu icon with a menu item select callback
void menu_select_callback(int index, void *ctx) {
  selected_message = index;
  send_message(menu_items[index].title);

  // Update UI
  menu_items[index].subtitle = "Sending message...";
  layer_mark_dirty(simple_menu_layer_get_layer(&simple_menu_layer));
}

// This initializes the menu upon window load
void show_response_menu() {
  // Although we already defined NUM_menu_items, you can define
  // an int as such to easily change the order of menu items later
  int num_a_items = 0;

  // This is an example of how you'd set a simple menu item
  menu_items[num_a_items++] = (SimpleMenuItem){
    // You should give each menu item a title and callback
    .title = "Okay.",
    .callback = menu_select_callback,
  };
  // The menu items appear in the order saved in the menu items array
  menu_items[num_a_items++] = (SimpleMenuItem){
    .title = "See you then.",
    .callback = menu_select_callback,
  };
  menu_items[num_a_items++] = (SimpleMenuItem){
    .title = "Lololol.",
    .callback = menu_select_callback,
  };

  // Bind the menu items to the corresponding menu sections
  menu_sections[0] = (SimpleMenuSection){
    .num_items = NUM_MENU_ITEMS,
    .items = menu_items,
  };

  // Now we prepare to initialize the simple menu layer
  // We need the bounds to specify the simple menu layer's viewport size
  // In this case, it'll be the same as the window's
  GRect bounds = window.layer.bounds;

  // Initialize the simple menu layer
  simple_menu_layer_init(&simple_menu_layer, bounds, &window, menu_sections, NUM_MENU_SECTIONS, NULL);

  // Add it to the window for display
  layer_add_child(&window.layer, simple_menu_layer_get_layer(&simple_menu_layer));
}

static void app_send_failed(DictionaryIterator* failed, AppMessageResult reason, void* context) {
  display_message("Message dropped");
}

static void app_send_succeeded(DictionaryIterator *sent, void *context) {
  menu_items[selected_message].subtitle = "Message Sent";
  layer_mark_dirty(simple_menu_layer_get_layer(&simple_menu_layer));
}

static void app_received_msg(DictionaryIterator* received, void* context) {
  // incoming message received
  //vibes_short_pulse();
  Tuple *message_tuple = dict_find(received, MESSAGE_KEY);

  if (message_tuple) {
    display_message(message_tuple->value->cstring);
  } else {
    display_message("Could not parse message");
  }
}

void select_single_click_handler(ClickRecognizerRef recognizer, Window *window) {
  hide_message_view();
  show_response_menu();
}

void setup_respond_button(ClickConfig **config, void *context) {
  config[BUTTON_ID_SELECT]->click.handler = (ClickHandler) select_single_click_handler;
}

void hide_message_view() {
  layer_set_hidden(&scroll_layer.layer, true);
}

void show_message_view(Window * me) {
  const GRect max_text_bounds = GRect(0, 0, 144, 2000);

  // Initialize the scroll layer
  scroll_layer_init(&scroll_layer, me->layer.bounds);

  // This binds the scroll layer to the window so that up and down map to scrolling
  // You may use scroll_layer_set_callbacks to add or override interactivity
  scroll_layer_set_click_config_onto_window(&scroll_layer, me);
  ScrollLayerCallbacks handlers = {
    .click_config_provider = &setup_respond_button
  };
  scroll_layer_set_callbacks(&scroll_layer, handlers);

  // Set the initial max size
  scroll_layer_set_content_size(&scroll_layer, max_text_bounds.size);

  // Initialize the text layer
  text_layer_init(&text_layer, max_text_bounds);
  display_message("Loading Message...");
  text_layer_set_text(&text_layer, scroll_text);

  // Change the font to a nice readable one
  // This is system font; you can inspect pebble_fonts.h for all system fonts
  // or you can take a look at feature_custom_font to add your own font
  text_layer_set_font(&text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));

  // Add the layers for display
  scroll_layer_add_child(&scroll_layer, &text_layer.layer);

  layer_add_child(&me->layer, &scroll_layer.layer);
}

void handle_init(AppContextRef ctx) {

  window_init(&window, "TextBack");
  window_set_window_handlers(&window, (WindowHandlers) {
    .load = show_message_view,
  });
  window_stack_push(&window, true /* Animated */);
  register_callbacks();
}

void pbl_main(void *params) {
  PebbleAppHandlers handlers = {
    .init_handler = &handle_init,
    .messaging_info = {
      .buffer_sizes = {
        .inbound = 256,
        .outbound = 256,
      }
    }
  };
  app_event_loop(params, &handlers);
}
