CREATE TABLE dishes (
  id   UUID PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE day_menus (
  day       DATE                        NOT NULL,
  menu_type TEXT                        NOT NULL,
  dish      UUID REFERENCES dishes (id) NOT NULL,
  PRIMARY KEY (day, menu_type, dish)
);