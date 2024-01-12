--
-- Database: `mutho_soft`
--

-- --------------------------------------------------------

--
-- Table structure for table `inav_edgeinfo`
--

CREATE TABLE `inav_edgeinfo` (
  `node1` int(4) NOT NULL,
  `node2` int(4) NOT NULL,
  `distance` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `inav_nodeinfo`
--

CREATE TABLE `inav_nodeinfo` (
  `id` int(11) NOT NULL,
  `building` varchar(64) NOT NULL,
  `label` varchar(128) NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `z` double NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `inav_shortpath`
--

CREATE TABLE `inav_shortpath` (
  `from_node` int(4) NOT NULL,
  `to_node` int(4) NOT NULL,
  `path` text DEFAULT NULL,
  `distance` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `inav_edgeinfo`
--
ALTER TABLE `inav_edgeinfo`
  ADD PRIMARY KEY (`node1`,`node2`);

--
-- Indexes for table `inav_nodeinfo`
--
ALTER TABLE `inav_nodeinfo`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `building` (`building`,`label`,`z`);

--
-- Indexes for table `inav_shortpath`
--
ALTER TABLE `inav_shortpath`
  ADD PRIMARY KEY (`from_node`,`to_node`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `inav_nodeinfo`
--
ALTER TABLE `inav_nodeinfo`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
