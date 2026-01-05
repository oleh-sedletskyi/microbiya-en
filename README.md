To see available tasks run:
```
bb tasks
```

To start blog locally run:
```
bb serve
```

To make the local server detect changes in the `content` directory run:
```
bb watch
```

`serve` and `watch` commands will make blog available locally at:
[http://localhost:7777](http://localhost:7777)

New posts should be added to the `content/posts` directory. Use markdown editor like Obsidian to edit `.md` files. 

To make the content available on the main site commit and push changes using GitKraken or commands:
```
git add <file-to-add>
git commit -m "Some message like: Adding fermented cabbage article"
git push
```
