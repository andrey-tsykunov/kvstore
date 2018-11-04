package com.andrey.playground.kvstore.impl

import com.andrey.playground.kvstore.api.KVStoreServiceComponents

class KVStoreServiceClientApp extends LagomClientApp("kvstore-svc")
  with KVStoreServiceComponents
