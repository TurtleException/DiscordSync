*Proper README will follow when v02 is done*

# TODO
- [x] Implement synchronized names
- [x] Fix incorrect amount of players displayed in Discord status when player leaves
  - Maybe also implement repeating task (*every 10 seconds or so*)
- [x] Parse embeds in minecraft
- [x] Parse attachments in container text
- [x] Soft wrap long container messages
- [ ] Implement automatic game lang file download
- [ ] Allow language reloads while the plugin is running
- [ ] (maybe) make the plugin reload-safe
- [ ] Implement reload for (user / block) config from file to allow manual changes
- [x] Prevent inner formatting for inline code & code blocks
- [x] Send quit messages when server stops
- [ ] Replace names in native event messages (in-game)
- [x] Explicitly update Discord presence on shutdown
  - maybe use *do not disturb*
- [ ] fix NPE when writing "*****"
- [ ] Implement more events
  - [ ] Special entity death (e.g. dogs)
- [ ] Implement `/delete` command
  - `/delete @<quick ref num>`
  - `/delete turtle <turtle id>`
  - `/delete discord <snowflake id>`
  - `/delete <beginning of message>` (use tab completer for cashed messages)
- [ ] Add newline character support for ingame chat
- [ ] *beep boop*

# Long-term goals
- [ ] Implement `/edit` command
  - usage:
    - `/edit @<quick ref num>`
    - `/edit turtle <turtle id>`
    - `/edit discord <snowflake id>`
    - `/edit <beginning of message>` (use tab completer for cashed messages)
  - Open prompt after command
- [ ] Hook Plugin to external program
  - [ ] report crashes
  - [ ] Implement server control via Discord
- [ ] Allow for more custom user settings
  - formatting
  - functionality
  - toggle auto naming
  - ...