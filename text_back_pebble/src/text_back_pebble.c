#include "pebble_os.h"
#include "pebble_app.h"
#include "pebble_fonts.h"


#define MY_UUID { 0xE8, 0xAE, 0x10, 0xA2, 0x2E, 0x91, 0x47, 0x3E, 0xB2, 0xFA, 0x6D, 0xD3, 0x82, 0xBA, 0xCD, 0x52 }
PBL_APP_INFO(MY_UUID,
             "TextBack", "MHacks",
             1, 0, /* App version */
             DEFAULT_MENU_ICON,
             APP_INFO_STANDARD_APP);


#define NUM_MENU_ITEMS 3
#define NUM_MENU_SECTIONS 1

#define MESSAGE_KEY 0x0


Window window;

// Response Menu
SimpleMenuLayer simple_menu_layer;
SimpleMenuSection menu_sections[NUM_MENU_SECTIONS];
SimpleMenuItem menu_items[NUM_MENU_ITEMS];

int selected_message;

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

// You can capture when the user selects a menu icon with a menu item select callback
void menu_select_callback(int index, void *ctx) {
  selected_message = index;
  send_message(menu_items[index].title);

  // Update UI
  menu_items[index].subtitle = "Sending message...";
  layer_mark_dirty(simple_menu_layer_get_layer(&simple_menu_layer));
}

// This initializes the menu upon window load
void window_load(Window *me) {

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
  GRect bounds = me->layer.bounds;

  // Initialize the simple menu layer
  simple_menu_layer_init(&simple_menu_layer, bounds, me, menu_sections, NUM_MENU_SECTIONS, NULL);

  // Add it to the window for display
  layer_add_child(&me->layer, simple_menu_layer_get_layer(&simple_menu_layer));
}


// Deinitialize resources on window unload that were initialized on window load
void window_unload(Window *me) {
}


void handle_init(AppContextRef ctx) {

  window_init(&window, "Demo");
  window_stack_push(&window, true /* Animated */);

  // Setup the window handlers
  window_set_window_handlers(&window, (WindowHandlers){
    .load = window_load,
    .unload = window_unload,
  });
}

void my_out_sent_handler(DictionaryIterator *sent, void *context) {
  // outgoing message was delivered
  // Update UI
  menu_items[selected_message].subtitle = "Message Sent";
  layer_mark_dirty(simple_menu_layer_get_layer(&simple_menu_layer));
}
void my_out_fail_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
  // outgoing message failed
  // Update UI
  menu_items[selected_message].subtitle = "Message Failed";
  layer_mark_dirty(simple_menu_layer_get_layer(&simple_menu_layer));
}
void my_in_rcv_handler(DictionaryIterator *received, void *context) {
  // incoming message received
}
void my_in_drp_handler(void *context, AppMessageResult reason) {
  // incoming message dropped
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
