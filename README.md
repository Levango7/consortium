# Consortium Development Guide

1. git subtree

```shell script
git remote add MonadJ https://github.com/Salpadding/MonadJ  # add remote
git subtree add --prefix=MonadJ MonadJ master # add folder MonadJ as subtree code directory
git subtree push --prefix=MonadJ MonadJ master # push subtree local change to remote
git subtree pull --prefix=MonadJ MonadJ master # pull from remote 
```

