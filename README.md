# jobs

A small API to handle a job-board type of application. `lein run` will start a server on port 3000.
REST routes:

- `GET /jobs`: Retrieve the jobs database.
- `POST /jobs`: Create a new job, needs the query arg params: `company`, `title`, and `keywords`.
- `GET /jobs/:id`: Retrieve a job by id.
- `PUT /jobs/:id`: Update details for a job. valid parameters are `company`, `title`, and `keywords`.
- `DELETE /jobs/:id`: Remove a job from the database



## Development Mode

### Start Cider from Emacs:

Put this in your Emacs config file:

```
(setq cider-cljs-lein-repl
	"(do (require 'figwheel-sidecar.repl-api)
         (figwheel-sidecar.repl-api/start-figwheel!)
         (figwheel-sidecar.repl-api/cljs-repl))")
```

Navigate to a clojurescript file and start a figwheel REPL with `cider-jack-in-clojurescript` or (`C-c M-J`)

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build

```
lein clean
lein uberjar
```

That should compile the clojurescript code first, and then create the standalone jar.

When you run the jar you can set the port the ring server will use by setting the environment variable PORT.
If it's not set, it will run on port 3000 by default.

To deploy to heroku, first create your app:

```
heroku create
```

Then deploy the application:

```
git push heroku master
```

To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```
